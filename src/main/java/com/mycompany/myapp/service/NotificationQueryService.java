package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.*;
import com.mycompany.myapp.domain.enumeration.BudgeStatus; // Sửa từ BudgeStatus thành BudgetStatus
import com.mycompany.myapp.domain.enumeration.NotificationType;
import com.mycompany.myapp.domain.enumeration.TransactionType;
import com.mycompany.myapp.repository.BudgetRepository;
import com.mycompany.myapp.repository.NotificationRepository;
import com.mycompany.myapp.repository.TransactionRepository;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.service.criteria.NotificationCriteria;
import jakarta.persistence.criteria.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.text.NumberFormat;

/**
 * Service for executing complex queries for {@link Notification} entities in
 * the database.
 * The main input is a {@link NotificationCriteria} which gets converted to
 * {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link Notification} which fulfills the
 * criteria.
 */
@Service
@Transactional(readOnly = true)
public class NotificationQueryService extends QueryService<Notification> {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationQueryService.class);
    private static final BigDecimal WARNING_THRESHOLD = new BigDecimal("0.85");
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("dd/MM/yyyy")
            .withZone(ZoneId.systemDefault());

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository; // Thêm BudgetRepository

    public NotificationQueryService(NotificationRepository notificationRepository, UserRepository userRepository,
            TransactionRepository transactionRepository, BudgetRepository budgetRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository; // Khởi tạo BudgetRepository
    }

    /**
     * Creates warning notifications for transactions matching active budgets if
     * total spent
     * reaches or exceeds 85% of the budget.
     *
     * @param userId      The ID of the user.
     * @param transaction The transaction to check.
     */
    @Transactional
    public void createWarningNotificationForTransaction(Long userId, Transaction transaction) {
        LOG.debug("Checking transaction for notification: {}", transaction);

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            LOG.warn("User not found for userId: {}", userId);
            return;
        }

        List<Budget> matchingBudgets = findMatchingBudgets(transaction);
        createNotificationsForBudgets(userOpt.get(), transaction, matchingBudgets);
    }

    /**
     * Creates notifications for each matching budget if the spending threshold is
     * reached.
     *
     * @param user            The user to associate with the notification.
     * @param transaction     The transaction to check.
     * @param matchingBudgets List of budgets to process.
     */
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

    /**
     * Calculates the total spent amount for a budget within its date range.
     *
     * @param budget      The budget to calculate for.
     * @param transaction The transaction to determine user and category.
     * @return Total spent amount.
     */
    private BigDecimal calculateTotalSpent(Budget budget, Transaction transaction) {
        return transactionRepository.sumAmountByCategoryIdAndUserIdAndDateRange(
                transaction.getCategory().getId(),
                transaction.getUser().getId(),
                budget.getStartDate(),
                budget.getEndDate());
    }

    /**
     * Determines if a warning notification should be created based on the spending
     * threshold.
     *
     * @param totalSpent  Total spent amount.
     * @param budget      The budget to check against.
     * @param transaction The transaction to check.
     * @return True if a warning should be created, false otherwise.
     */
    private boolean shouldCreateWarning(BigDecimal totalSpent, Budget budget, Transaction transaction) {
        BigDecimal budgetThreshold = budget.getBudgetAmount().multiply(WARNING_THRESHOLD);
        return totalSpent.compareTo(budgetThreshold) >= 0
                && totalSpent.compareTo(budget.getBudgetAmount()) < 0
                && transaction.getTransactionType() == TransactionType.EXPENSE;
    }

    /**
     * Creates and saves a budget warning notification.
     *
     * @param user        The user to associate with the notification.
     * @param budget      The budget that triggered the warning.
     * @param totalSpent  Total spent amount.
     * @param transaction The transaction that triggered the check.
     */
    private void createBudgetWarningNotification(User user, Budget budget, BigDecimal totalSpent,
            Transaction transaction) {
        LOG.info(
                "Budget ID: {} triggered a warning because total spent {} reaches or exceeds 85% of budget amount {}",
                budget.getId(), totalSpent, budget.getBudgetAmount());

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
                budget.getId(), totalSpent, budget.getBudgetAmount());
    }

    /**
     * Creates notifications for transactions matching active budgets if total
     * amount
     * exceeds or completes the budget.
     *
     * @param userId      The ID of the user.
     * @param transaction The transaction to check.
     */
    @Transactional
    public void createNotificationForTransaction(Long userId, Transaction transaction) {
        LOG.debug("Checking transaction for notification: {}", transaction);

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            LOG.warn("User not found for userId: {}", userId);
            return;
        }

        User user = userOpt.get();
        List<Budget> matchingBudgets = findMatchingBudgets(transaction);
        processBudgetsForNotification(user, transaction, matchingBudgets);
    }

    /**
     * Finds budgets that match the transaction's category, user, and date range.
     *
     * @param transaction The transaction to match budgets against.
     * @return List of matching budgets.
     */
    private List<Budget> findMatchingBudgets(Transaction transaction) {
        return notificationRepository.findMatchingBudgetsWithTransactionDate(
                transaction.getCategory().getId(),
                transaction.getUser().getId(),
                transaction.getTransactionDate());
    }

    /**
     * Processes each matching budget to create appropriate notifications.
     *
     * @param user            The user to associate with the notification.
     * @param transaction     The transaction to check.
     * @param matchingBudgets List of budgets to process.
     */
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

    /**
     * Handles notification creation for an income transaction.
     *
     * @param user        The user to associate with the notification.
     * @param budget      The budget that triggered the notification.
     * @param totalSpent  Total spent amount.
     * @param transaction The transaction to check.
     */
    private void handleIncomeTransaction(User user, Budget budget, BigDecimal totalSpent, Transaction transaction) {
        if (totalSpent.compareTo(budget.getBudgetAmount()) > 0) {
            String message = formatIncomeMessage(budget, transaction);
            createNotification(user, budget, message, NotificationType.COMPLETE);
            updateBudgetStatus(budget, BudgeStatus.ENDED);
            LOG.debug("Notification created for INCOME transaction: {} with budget: {}",
                    transaction.getId(), budget.getId());
        } else {
            logNoNotificationCreated(budget, totalSpent);
        }
    }

    /**
     * Handles notification creation for an expense transaction.
     *
     * @param user        The user to associate with the notification.
     * @param budget      The budget that triggered the notification.
     * @param totalSpent  Total spent amount.
     * @param transaction The transaction to check.
     */
    private void handleExpenseTransaction(User user, Budget budget, BigDecimal totalSpent, Transaction transaction) {
        if (totalSpent.compareTo(budget.getBudgetAmount()) > 0) {
            LOG.info("Budget ID: {} status updated to ENDED because total spent {} exceeds budget amount {}",
                    budget.getId(), totalSpent, budget.getBudgetAmount());
            String message = formatExpenseMessage(budget, totalSpent, transaction.getCategory());
            createNotification(user, budget, message, NotificationType.BUDGET_EXCEEDED);
            updateBudgetStatus(budget, BudgeStatus.ENDED);
            LOG.debug("Notification created for budget: {} because total spent {} exceeds budget amount {}",
                    budget.getId(), totalSpent, budget.getBudgetAmount());
        } else {
            logNoNotificationCreated(budget, totalSpent);
        }
    }

    /**
     * Formats the warning message for the notification.
     *
     * @param budget   The budget to format the message for.
     * @param category The category associated with the budget.
     * @return Formatted warning message.
     */
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
                category.getCategoryName());
    }

    /**
     * Formats the notification message for an income transaction.
     *
     * @param budget      The budget to format the message for.
     * @param transaction The transaction to include the date.
     * @return Formatted message.
     */
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
                budget.getCategory().getCategoryName());
    }

    /**
     * Formats the notification message for an expense transaction.
     *
     * @param budget     The budget to format the message for.
     * @param totalSpent Total spent amount.
     * @param category   The category associated with the budget.
     * @return Formatted message.
     */
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
                category.getCategoryName());
    }

    /**
     * Creates and saves a notification with the given details.
     *
     * @param user             The user to associate with the notification.
     * @param budget           The budget that triggered the notification.
     * @param message          The message content.
     * @param notificationType The type of notification.
     */
    private void createNotification(User user, Budget budget, String message, NotificationType notificationType) {
        Notification notification = new Notification()
                .message(message)
                .notificationType(notificationType)
                .isRead(false)
                .createdAt(Instant.now())
                .user(user);
        notificationRepository.save(notification);
    }

    /**
     * Updates the budget status and saves the changes.
     *
     * @param budget The budget to update.
     * @param status The new status to set.
     */
    private void updateBudgetStatus(Budget budget, BudgeStatus status) {
        budget.setStatus(status);
        budgetRepository.save(budget);
    }

    /**
     * Logs when a notification is not created for a budget.
     *
     * @param budget     The budget that was checked.
     * @param totalSpent Total spent amount.
     */
    private void logNoNotificationCreated(Budget budget, BigDecimal totalSpent) {
        LOG.debug("No notification created for budget: {} (total spent: {} is within budget: {})",
                budget.getId(), totalSpent, budget.getBudgetAmount());
    }

    /**
     * Return a {@link Page} of {@link Notification} which matches the criteria from
     * the database.
     *
     * @param criteria The object which holds all the filters, which the entities
     *                 should match.
     * @param page     The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<Notification> findByCriteria(NotificationCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Notification> specification = createSpecification(criteria);
        return notificationRepository.findAll(specification, page);
    }

    /**
     * Return the number of matching entities in the database.
     *
     * @param criteria The object which holds all the filters, which the entities
     *                 should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(NotificationCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Notification> specification = createSpecification(criteria);
        return notificationRepository.count(specification);
    }

    /**
     * Function to convert {@link NotificationCriteria} to a {@link Specification}
     *
     * @param criteria The object which holds all the filters, which the entities
     *                 should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Notification> createSpecification(NotificationCriteria criteria) {
        Specification<Notification> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), Notification_.id));
            }
            if (criteria.getMessage() != null) {
                specification = specification
                        .and(buildStringSpecification(criteria.getMessage(), Notification_.message));
            }
            if (criteria.getNotificationType() != null) {
                specification = specification
                        .and(buildSpecification(criteria.getNotificationType(), Notification_.notificationType));
            }
            if (criteria.getIsRead() != null) {
                specification = specification.and(buildSpecification(criteria.getIsRead(), Notification_.isRead));
            }
            if (criteria.getCreatedAt() != null) {
                specification = specification
                        .and(buildRangeSpecification(criteria.getCreatedAt(), Notification_.createdAt));
            }
            if (criteria.getUserId() != null) {
                specification = specification.and(
                        buildSpecification(criteria.getUserId(),
                                root -> root.join(Notification_.user, JoinType.LEFT).get(User_.id)));
            }
        }
        return specification;
    }
}