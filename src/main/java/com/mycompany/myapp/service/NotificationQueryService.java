package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.*; // for static metamodels
import com.mycompany.myapp.domain.enumeration.NotificationType;
import com.mycompany.myapp.repository.NotificationRepository;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.repository.TransactionRepository;
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

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public NotificationQueryService(NotificationRepository notificationRepository, UserRepository userRepository,
            TransactionRepository transactionRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Create notifications for transactions matching active budgets if total amount
     * exceeds budget.
     * 
     * @param userId      The ID of the user.
     * @param transaction The transaction to check.
     */
    @Transactional
    public void createNotificationForTransaction(Long userId, Transaction transaction) {
        LOG.debug("Checking transaction for notification: {}", transaction);

        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            LOG.warn("User not found for userId: {}", userId);
            return;
        }
        User user = userOpt.get();

        List<Budget> matchingBudgets = notificationRepository.findMatchingBudgetsWithTransactionDate(
                transaction.getCategory().getId(),
                userId,
                transaction.getTransactionDate());

        for (Budget budget : matchingBudgets) {
            // Tính tổng amount của tất cả giao dịch trong khoảng thời gian của budget
            BigDecimal totalSpent = transactionRepository.sumAmountByCategoryIdAndUserIdAndDateRange(
                    transaction.getCategory().getId(),
                    userId,
                    budget.getStartDate(),
                    budget.getEndDate());

            // Kiểm tra nếu tổng vượt quá budgetAmount
            if (totalSpent.compareTo(budget.getBudgetAmount()) > 0) {
                // Điều chỉnh thông điệp để khớp với regex ở frontend
                NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
                String budgetAmountFormatted = numberFormat.format(budget.getBudgetAmount());
                String exceededAmountFormatted = numberFormat.format(totalSpent.subtract(budget.getBudgetAmount()));
                String message = String.format(
                        "Bạn đã chi tiêu vượt quá ngân sách có khối lượng %s₫ với số tiền vượt là %s₫ trong khoảng thời gian đã thiết lập từ ngày %s đến ngày %s của danh mục %s %s",
                        budgetAmountFormatted,
                        exceededAmountFormatted,
                        budget.getStartDate(),
                        budget.getEndDate(),
                        budget.getCategory().getCategoryIcon(),
                        budget.getCategory().getCategoryName());

                Notification notification = new Notification()
                        .message(message)
                        .notificationType(NotificationType.BUDGET_EXCEEDED)
                        .isRead(false)
                        .createdAt(Instant.now())
                        .user(user);

                notificationRepository.save(notification);
                LOG.debug("Notification created for budget: {} because total spent {} exceeds budget amount {}",
                        budget.getId(), totalSpent, budget.getBudgetAmount());
            } else {
                LOG.debug("No notification created for budget: {} (total spent: {} is within budget: {})",
                        budget.getId(), totalSpent, budget.getBudgetAmount());
            }
        }
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
            // This has to be called first, because the distinct method returns null
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
