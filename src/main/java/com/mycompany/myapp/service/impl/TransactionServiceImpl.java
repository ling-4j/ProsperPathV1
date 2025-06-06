package com.mycompany.myapp.service.impl;

import com.mycompany.myapp.domain.Transaction;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.repository.TransactionRepository;
import com.mycompany.myapp.service.NotificationService;
import com.mycompany.myapp.service.SummaryService;
import com.mycompany.myapp.service.TransactionService;
import com.mycompany.myapp.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing
 * {@link com.mycompany.myapp.domain.Transaction}.
 */
@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final SummaryService summaryService;
    private final NotificationService notificationService;

    public TransactionServiceImpl(
        TransactionRepository transactionRepository,
        UserService userService,
        SummaryService summaryService,
        NotificationService notificationService
    ) {
        this.transactionRepository = transactionRepository;
        this.userService = userService;
        this.summaryService = summaryService;
        this.notificationService = notificationService;
    }

    @Override
    public Transaction save(Transaction transaction) {
        LOG.debug("Request to save Transaction : {}", transaction);
        if (transaction.getUser() == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                userService.getUserWithAuthorities().ifPresent(transaction::setUser);
            }
        }

        transaction.setCreatedAt(Instant.now());
        transaction.setUpdatedAt(Instant.now());
        transaction.setTransactionDate(transaction.getTransactionDate());
        Transaction savedTransaction = transactionRepository.save(transaction);
        Long userId = savedTransaction.getUser().getId();
        summaryService.updateSummaryForTransaction(userId, null, savedTransaction);
        if (savedTransaction.getCategory() != null) {
            notificationService.createNotificationForTransaction(userId, savedTransaction);
            notificationService.createWarningNotificationForTransaction(userId, savedTransaction);
        }
        return savedTransaction;
    }

    @Override
    public Transaction update(Transaction transaction) {
        LOG.debug("Request to update Transaction : {}", transaction);
        Transaction existingTransaction = transactionRepository
            .findById(transaction.getId())
            .orElseThrow(() -> new EntityNotFoundException("Transaction not found with id: " + transaction.getId()));
        Transaction oldTransaction = cloneTransaction(existingTransaction);

        transaction.setUpdatedAt(Instant.now());
        transaction.setTransactionDate(transaction.getTransactionDate());
        Transaction updatedTransaction = transactionRepository.save(transaction);
        Long userId = updatedTransaction.getUser().getId();
        summaryService.updateSummaryForTransaction(userId, oldTransaction, updatedTransaction);
        if (transaction.getCategory() != null) {
            notificationService.createNotificationForTransaction(userId, transaction);
            notificationService.createWarningNotificationForTransaction(userId, transaction);
        }
        return updatedTransaction;
    }

    private Transaction cloneTransaction(Transaction t) {
        Transaction clone = new Transaction();
        clone.setId(t.getId());
        clone.setAmount(t.getAmount());
        clone.setTransactionType(t.getTransactionType());
        clone.setDescription(t.getDescription());
        clone.setTransactionDate(t.getTransactionDate());
        clone.setUser(t.getUser());
        clone.setCategory(t.getCategory());
        return clone;
    }

    @Override
    public Optional<Transaction> partialUpdate(Transaction transaction) {
        LOG.debug("Request to partially update Transaction : {}", transaction);

        return transactionRepository
            .findById(transaction.getId())
            .map(existingTransaction -> {
                if (transaction.getAmount() != null) {
                    existingTransaction.setAmount(transaction.getAmount());
                }
                if (transaction.getTransactionType() != null) {
                    existingTransaction.setTransactionType(transaction.getTransactionType());
                }
                if (transaction.getDescription() != null) {
                    existingTransaction.setDescription(transaction.getDescription());
                }
                if (transaction.getTransactionDate() != null) {
                    existingTransaction.setTransactionDate(transaction.getTransactionDate());
                }
                if (transaction.getCreatedAt() != null) {
                    existingTransaction.setCreatedAt(transaction.getCreatedAt());
                }
                if (transaction.getUpdatedAt() != null) {
                    existingTransaction.setUpdatedAt(transaction.getUpdatedAt());
                }

                return existingTransaction;
            })
            .map(transactionRepository::save);
    }

    public Page<Transaction> findAllWithEagerRelationships(Pageable pageable) {
        return transactionRepository.findAllWithEagerRelationships(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Transaction> findOne(Long id) {
        LOG.debug("Request to get Transaction : {}", id);
        return transactionRepository.findOneWithEagerRelationships(id);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Transaction : {}", id);
        Transaction transaction = transactionRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Transaction not found with id: " + id));

        validateDeletePermission(transaction);

        Long userId = transaction.getUser().getId();
        summaryService.updateSummaryForTransaction(userId, transaction, null);
        transactionRepository.deleteById(id);
    }

    private void validateDeletePermission(Transaction transaction) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            User currentUser = userService.getUserWithAuthorities().orElseThrow(() -> new SecurityException("Current user not found"));
            transaction.setUser(currentUser);
            if (!transaction.getUser().getId().equals(currentUser.getId())) {
                LOG.warn(
                    "User {} attempted to delete transaction {} that does not belong to them",
                    currentUser.getId(),
                    transaction.getId()
                );
                throw new SecurityException("You are not authorized to delete this transaction");
            }
        }
    }

    @Override
    public List<Transaction> findByUserId(Long userId) {
        LOG.debug("Request to get Transactions by userId : {}", userId);
        return transactionRepository.findByUserId(userId);
    }
}
