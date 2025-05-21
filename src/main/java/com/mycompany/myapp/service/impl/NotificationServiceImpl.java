package com.mycompany.myapp.service.impl;

import com.mycompany.myapp.domain.*;
import com.mycompany.myapp.domain.enumeration.BudgeStatus;
import com.mycompany.myapp.domain.enumeration.NotificationType;
import com.mycompany.myapp.domain.enumeration.TransactionType;
import com.mycompany.myapp.repository.BudgetRepository;
import com.mycompany.myapp.repository.NotificationRepository;
import com.mycompany.myapp.repository.TransactionRepository;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.service.NotificationService;
import com.mycompany.myapp.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private static final BigDecimal WARNING_THRESHOLD = new BigDecimal("0.85");
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault());

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final UserService userService;

    public NotificationServiceImpl(
        NotificationRepository notificationRepository,
        UserRepository userRepository,
        TransactionRepository transactionRepository,
        BudgetRepository budgetRepository,
        UserService userService
    ) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
        this.userService = userService;
    }

    @Override
    public Notification save(Notification notification) {
        LOG.debug("Request to save Notification : {}", notification);
        if (notification.getUser() == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Optional<User> currentUser = userService.getUserWithAuthorities();
                currentUser.ifPresent(notification::setUser);
            }
        }
        return notificationRepository.save(notification);
    }

    @Override
    public Notification update(Notification notification) {
        LOG.debug("Request to update Notification : {}", notification);
        if (notification.getId() == null) {
            throw new IllegalArgumentException("Notification ID cannot be null for update");
        }
        return notificationRepository.save(notification);
    }

    @Override
    public Optional<Notification> partialUpdate(Notification notification) {
        LOG.debug("Request to partially update Notification : {}", notification);

        return notificationRepository
            .findById(notification.getId())
            .map(existingNotification -> {
                if (notification.getMessage() != null) {
                    existingNotification.setMessage(notification.getMessage());
                }
                if (notification.getNotificationType() != null) {
                    existingNotification.setNotificationType(notification.getNotificationType());
                }
                if (notification.getIsRead() != null) {
                    existingNotification.setIsRead(notification.getIsRead());
                }
                if (notification.getCreatedAt() != null) {
                    existingNotification.setCreatedAt(notification.getCreatedAt());
                }
                return existingNotification;
            })
            .map(notificationRepository::save);
    }

    @Override
    public Page<Notification> findAllWithEagerRelationships(Pageable pageable) {
        return notificationRepository.findAllWithEagerRelationships(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Notification> findOne(Long id) {
        LOG.debug("Request to get Notification : {}", id);
        return notificationRepository.findOneWithEagerRelationships(id);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Notification : {}", id);
        notificationRepository.deleteById(id);
    }

    @Override
    public void createNotificationForTransaction(Long userId, Transaction transaction) {
        LOG.debug("Checking transaction for notification: {}", transaction);

        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found for userId: " + userId));
        List<Budget> matchingBudgets = findMatchingBudgets(transaction);
        processBudgetsForNotification(user, transaction, matchingBudgets);
    }

    @Override
    public void createWarningNotificationForTransaction(Long userId, Transaction transaction) {
        LOG.debug("Checking transaction for warning notification: {}", transaction);

        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found for userId: " + userId));
        List<Budget> matchingBudgets = findMatchingBudgets(transaction);
        createNotificationsForBudgets(user, transaction, matchingBudgets);
    }

    private void createNotificationsForBudgets(User user, Transaction transaction, List<Budget> matchingBudgets) {
        for (Budget budget : matchingBudgets) {
            BigDecimal totalSpent = calculateTotalSpent(budget, transaction);
            if (shouldCreateWarning(totalSpent, budget, transaction)) {
                createBudgetWarningNotification(user, budget, totalSpent, transaction);
            } else {
                logNoNotificationCreated(budget, totalSpent);
            }
        }
    }

    private BigDecimal calculateTotalSpent(Budget budget, Transaction transaction) {
        return transactionRepository.sumAmountByCategoryIdAndUserIdAndDateRange(
            transaction.getCategory().getId(),
            transaction.getUser().getId(),
            budget.getStartDate(),
            budget.getEndDate()
        );
    }

    private boolean shouldCreateWarning(BigDecimal totalSpent, Budget budget, Transaction transaction) {
        BigDecimal budgetThreshold = budget.getBudgetAmount().multiply(WARNING_THRESHOLD);
        return (
            totalSpent.compareTo(budgetThreshold) >= 0 &&
            totalSpent.compareTo(budget.getBudgetAmount()) < 0 &&
            transaction.getTransactionType() == TransactionType.EXPENSE
        );
    }

    private void createBudgetWarningNotification(User user, Budget budget, BigDecimal totalSpent, Transaction transaction) {
        LOG.info(
            "Budget ID: {} triggered a warning because total spent {} reaches or exceeds 85% of budget amount {}",
            budget.getId(),
            totalSpent,
            budget.getBudgetAmount()
        );

        String message = formatWarningMessage(budget, transaction.getCategory());
        Notification notification = new Notification()
            .message(message)
            .notificationType(NotificationType.WARNING)
            .isRead(false)
            .createdAt(Instant.now())
            .user(user);

        notificationRepository.save(notification);
        LOG.debug(
            "Notification created for budget: {} because total spent {} reaches or exceeds 85% of budget amount {}",
            budget.getId(),
            totalSpent,
            budget.getBudgetAmount()
        );
    }

    private List<Budget> findMatchingBudgets(Transaction transaction) {
        return notificationRepository.findMatchingBudgetsWithTransactionDate(
            transaction.getCategory().getId(),
            transaction.getUser().getId(),
            transaction.getTransactionDate()
        );
    }

    private void processBudgetsForNotification(User user, Transaction transaction, List<Budget> matchingBudgets) {
        for (Budget budget : matchingBudgets) {
            BigDecimal totalSpent = calculateTotalSpent(budget, transaction);
            if (transaction.getTransactionType() == TransactionType.INCOME) {
                handleIncomeTransaction(user, budget, totalSpent, transaction);
            } else if (transaction.getTransactionType() == TransactionType.EXPENSE) {
                handleExpenseTransaction(user, budget, totalSpent, transaction);
            } else {
                logNoNotificationCreated(budget, totalSpent);
            }
        }
    }

    private void handleIncomeTransaction(User user, Budget budget, BigDecimal totalSpent, Transaction transaction) {
        if (totalSpent.compareTo(budget.getBudgetAmount()) >= 0) {
            String message = formatIncomeMessage(budget, transaction);
            createNotification(user, budget, message, NotificationType.COMPLETE);
            updateBudgetStatus(budget, BudgeStatus.ENDED);
            LOG.debug("Notification created for INCOME transaction: {} with budget: {}", transaction.getId(), budget.getId());
        } else {
            logNoNotificationCreated(budget, totalSpent);
        }
    }

    private void handleExpenseTransaction(User user, Budget budget, BigDecimal totalSpent, Transaction transaction) {
        if (totalSpent.compareTo(budget.getBudgetAmount()) >= 0) {
            LOG.info(
                "Budget ID: {} status updated to ENDED because total spent {} exceeds budget amount {}",
                budget.getId(),
                totalSpent,
                budget.getBudgetAmount()
            );
            String message = formatExpenseMessage(budget, totalSpent, transaction.getCategory());
            createNotification(user, budget, message, NotificationType.BUDGET_EXCEEDED);
            updateBudgetStatus(budget, BudgeStatus.ENDED);
            LOG.debug(
                "Notification created for budget: {} because total spent {} exceeds budget amount {}",
                budget.getId(),
                totalSpent,
                budget.getBudgetAmount()
            );
        } else {
            logNoNotificationCreated(budget, totalSpent);
        }
    }

    private String formatWarningMessage(Budget budget, Category category) {
        String budgetAmountFormatted = NUMBER_FORMAT.format(budget.getBudgetAmount());
        String startDateFormatted = DATE_FORMATTER.format(budget.getStartDate());
        String endDateFormatted = DATE_FORMATTER.format(budget.getEndDate());
        return String.format(
            "Cảnh báo! Bạn sắp vượt chi tiêu ngân sách có khối lượng %s₫ trong khoảng thời gian đã thiết lập từ %s đến %s của danh mục %s %s",
            budgetAmountFormatted,
            startDateFormatted,
            endDateFormatted,
            category.getCategoryIcon(),
            category.getCategoryName()
        );
    }

    private String formatIncomeMessage(Budget budget, Transaction transaction) {
        String budgetAmountFormatted = NUMBER_FORMAT.format(budget.getBudgetAmount());
        String startDateFormatted = DATE_FORMATTER.format(budget.getStartDate());
        String endDateFormatted = DATE_FORMATTER.format(budget.getEndDate());
        String transactionDateFormatted = DATE_FORMATTER.format(transaction.getTransactionDate());
        return String.format(
            "Bạn đã hoàn thành mục tiêu ngân sách với khối lượng %s₫ trong khoảng thời gian đã thiết lập từ %s đến %s vào ngày %s của danh mục %s %s",
            budgetAmountFormatted,
            startDateFormatted,
            endDateFormatted,
            transactionDateFormatted,
            budget.getCategory().getCategoryIcon(),
            budget.getCategory().getCategoryName()
        );
    }

    private String formatExpenseMessage(Budget budget, BigDecimal totalSpent, Category category) {
        String budgetAmountFormatted = NUMBER_FORMAT.format(budget.getBudgetAmount());
        String exceededAmountFormatted = NUMBER_FORMAT.format(totalSpent.subtract(budget.getBudgetAmount()));
        String startDateFormatted = DATE_FORMATTER.format(budget.getStartDate());
        String endDateFormatted = DATE_FORMATTER.format(budget.getEndDate());
        return String.format(
            "Bạn đã chi tiêu vượt quá ngân sách có khối lượng %s₫ với số tiền vượt là %s₫ trong khoảng thời gian đã thiết lập từ %s đến %s của danh mục %s %s",
            budgetAmountFormatted,
            exceededAmountFormatted,
            startDateFormatted,
            endDateFormatted,
            category.getCategoryIcon(),
            category.getCategoryName()
        );
    }

    private void createNotification(User user, Budget budget, String message, NotificationType notificationType) {
        Notification notification = new Notification()
            .message(message)
            .notificationType(notificationType)
            .isRead(false)
            .createdAt(Instant.now())
            .user(user);
        notificationRepository.save(notification);
    }

    private void updateBudgetStatus(Budget budget, BudgeStatus status) {
        budget.setStatus(status);
        budgetRepository.save(budget);
    }

    private void logNoNotificationCreated(Budget budget, BigDecimal totalSpent) {
        LOG.debug(
            "No notification created for budget: {} (total spent: {} is within budget: {})",
            budget.getId(),
            totalSpent,
            budget.getBudgetAmount()
        );
    }
}
