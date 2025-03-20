package com.mycompany.myapp.service.impl;

import com.mycompany.myapp.domain.Transaction;
import com.mycompany.myapp.repository.TransactionRepository;
import com.mycompany.myapp.service.TransactionService;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.mycompany.myapp.domain.Transaction}.
 */
@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private final TransactionRepository transactionRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Transaction save(Transaction transaction) {
        LOG.debug("Request to save Transaction : {}", transaction);
        return transactionRepository.save(transaction);
    }

    @Override
    public Transaction update(Transaction transaction) {
        LOG.debug("Request to update Transaction : {}", transaction);
        return transactionRepository.save(transaction);
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
        transactionRepository.deleteById(id);
    }
}
