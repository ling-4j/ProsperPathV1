package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.*; // for static metamodels
import com.mycompany.myapp.domain.enumeration.PeriodType;
import com.mycompany.myapp.domain.enumeration.TransactionType;
import com.mycompany.myapp.repository.SummaryRepository;
import com.mycompany.myapp.repository.TransactionRepository;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.service.criteria.SummaryCriteria;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.JoinType;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link Summary} entities in the
 * database.
 * The main input is a {@link SummaryCriteria} which gets converted to
 * {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link Summary} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class SummaryQueryService extends QueryService<Summary> {

    private static final Logger LOG = LoggerFactory.getLogger(SummaryQueryService.class);

    private final SummaryRepository summaryRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository; // Thêm TransactionRepository

    @PersistenceContext
    private EntityManager entityManager;

    public SummaryQueryService(
            SummaryRepository summaryRepository,
            UserRepository userRepository,
            TransactionRepository transactionRepository) {
        this.summaryRepository = summaryRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository; // Khởi tạo TransactionRepository
    }

    /**
     * Lấy dữ liệu chi tiết tài chính (thu nhập, chi phí, lợi nhuận) theo kỳ cho
     * biểu đồ.
     *
     * @param userId ID của người dùng
     * @param period Loại kỳ (WEEK, MONTH, YEAR)
     * @return Map chứa nhãn (labels) và dữ liệu (incomeData, expenseData,
     *         progressRateData)
     */
    public Map<String, Object> getDetailedFinancialData(Long userId, String period) {
        // Xác định khoảng thời gian dựa trên period, sử dụng múi giờ UTC
        LocalDate now = LocalDate.now(ZoneId.of("UTC"));
        LocalDate startDate;
        LocalDate endDate;

        switch (period.toUpperCase()) {
            case "WEEK":
                startDate = now.minusDays(6); // 7 ngày trước
                endDate = now;
                break;
            case "MONTH":
                startDate = now.withDayOfMonth(1); // Đầu tháng
                endDate = now.withDayOfMonth(now.lengthOfMonth()); // Cuối tháng
                break;
            case "YEAR":
                startDate = now.minusYears(1).withMonth(5).withDayOfMonth(1); // Bắt đầu từ tháng 5 năm trước
                endDate = now.withDayOfMonth(now.lengthOfMonth()); // Đến tháng hiện tại (4/2025)
                break;
            default:
                throw new IllegalArgumentException("Invalid period: " + period);
        }

        Instant startInstant = startDate.atStartOfDay(ZoneId.of("UTC")).toInstant();
        Instant endInstant = endDate.atTime(23, 59, 59).atZone(ZoneId.of("UTC")).toInstant();

        // Lấy danh sách giao dịch
        List<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateBetween(userId,
                startInstant, endInstant);
        LOG.info(
                "Transactions found for userId: {}, period: {}, from: {}, to: {}, count: {}",
                userId,
                period,
                startInstant,
                endInstant,
                transactions.size());
        for (Transaction transaction : transactions) {
            LOG.debug(
                    "Transaction: id={}, userId={}, date={}, type={}, amount={}",
                    transaction.getId(),
                    transaction.getUser().getId(),
                    transaction.getTransactionDate(),
                    transaction.getTransactionType(),
                    transaction.getAmount());
        }

        // Tạo dữ liệu cho biểu đồ
        List<String> labels = new ArrayList<>();
        List<BigDecimal> incomeData = new ArrayList<>();
        List<BigDecimal> expenseData = new ArrayList<>();
        List<BigDecimal> progressRateData = new ArrayList<>();

        if (period.equalsIgnoreCase("WEEK")) {
            for (int i = 0; i < 7; i++) {
                LocalDate date = startDate.plusDays(i);
                labels.add(date.format(DateTimeFormatter.ofPattern("dd/MM")));

                BigDecimal income = BigDecimal.ZERO;
                BigDecimal expense = BigDecimal.ZERO;
                for (Transaction transaction : transactions) {
                    LocalDate transactionDate = transaction.getTransactionDate().atZone(ZoneId.of("UTC")).toLocalDate();
                    if (transactionDate.equals(date)) {
                        if (transaction.getTransactionType() == TransactionType.INCOME) {
                            income = income.add(transaction.getAmount());
                        } else if (transaction.getTransactionType() == TransactionType.EXPENSE) {
                            expense = expense.add(transaction.getAmount());
                        }
                    }
                }
                incomeData.add(income);
                expenseData.add(expense);
                progressRateData.add(income.subtract(expense));
            }
        } else if (period.equalsIgnoreCase("MONTH")) {
            // Dùng ngày đầu tiên và cuối cùng của tháng dựa trên giao dịch hoặc thời gian
            // hiện tại
            LocalDate monthStart = startDate;
            LocalDate monthEnd = endDate;
            if (!transactions.isEmpty()) {
                // Lấy tháng từ giao dịch đầu tiên để đảm bảo đúng tháng
                LocalDate firstTransactionDate = transactions.get(0).getTransactionDate().atZone(ZoneId.of("UTC"))
                        .toLocalDate();
                monthStart = firstTransactionDate.withDayOfMonth(1);
                monthEnd = firstTransactionDate.withDayOfMonth(firstTransactionDate.lengthOfMonth());
            }

            int daysInMonth = monthEnd.getDayOfMonth();
            for (int i = 1; i <= daysInMonth; i += 5) {
                LocalDate startRange = monthStart.withDayOfMonth(i);
                LocalDate endRange = monthStart.withDayOfMonth(Math.min(i + 4, daysInMonth));
                labels.add(
                        startRange.format(DateTimeFormatter.ofPattern("dd/MM")) + "-"
                                + endRange.format(DateTimeFormatter.ofPattern("dd/MM")));

                BigDecimal income = BigDecimal.ZERO;
                BigDecimal expense = BigDecimal.ZERO;
                for (Transaction transaction : transactions) {
                    LocalDate transactionDate = transaction.getTransactionDate().atZone(ZoneId.of("UTC")).toLocalDate();
                    LOG.debug("Transaction date: {}, startRange: {}, endRange: {}", transactionDate, startRange,
                            endRange);
                    if (!transactionDate.isBefore(startRange) && !transactionDate.isAfter(endRange)) {
                        if (transaction.getTransactionType() == TransactionType.INCOME) {
                            income = income.add(transaction.getAmount());
                        } else if (transaction.getTransactionType() == TransactionType.EXPENSE) {
                            expense = expense.add(transaction.getAmount());
                        }
                    }
                }
                incomeData.add(income);
                expenseData.add(expense);
                progressRateData.add(income.subtract(expense));
            }
        } else if (period.equalsIgnoreCase("YEAR")) {
            LocalDate current = startDate;
            while (!current.isAfter(endDate)) {
                labels.add(current.format(DateTimeFormatter.ofPattern("yyyy-MM")));

                BigDecimal income = BigDecimal.ZERO;
                BigDecimal expense = BigDecimal.ZERO;
                for (Transaction transaction : transactions) {
                    LocalDate transactionDate = transaction.getTransactionDate().atZone(ZoneId.of("UTC")).toLocalDate();
                    LOG.debug("Transaction date: {}, current month: {}-{}", transactionDate, current.getYear(),
                            current.getMonthValue());
                    if (transactionDate.getYear() == current.getYear()
                            && transactionDate.getMonthValue() == current.getMonthValue()) {
                        if (transaction.getTransactionType() == TransactionType.INCOME) {
                            income = income.add(transaction.getAmount());
                        } else if (transaction.getTransactionType() == TransactionType.EXPENSE) {
                            expense = expense.add(transaction.getAmount());
                        }
                    }
                }
                incomeData.add(income);
                expenseData.add(expense);
                progressRateData.add(income.subtract(expense));
                current = current.plusMonths(1);
            }
        }

        // Log dữ liệu trước khi trả về
        LOG.debug(
                "Chart data for userId: {}, period: {}: labels={}, incomeData={}, expenseData={}, progressRateData={}",
                userId,
                period,
                labels,
                incomeData,
                expenseData,
                progressRateData);

        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("incomeData", incomeData);
        result.put("expenseData", expenseData);
        result.put("progressRateData", progressRateData);
        return result;
    }

    /**
     * Lấy bản ghi Summary cho một kỳ cụ thể của người dùng.
     *
     * @param userId      ID của người dùng
     * @param periodType  Loại kỳ (WEEK, MONTH, YEAR)
     * @param periodValue Giá trị kỳ (ví dụ: "2023-03" cho tháng 3/2023)
     * @return Summary hoặc null nếu không tìm thấy
     */
    public Summary getSummaryForPeriod(Long userId, String periodType, String periodValue) {
        LOG.debug("Getting summary for userId: {}, periodType: {}, periodValue: {}", userId, periodType, periodValue);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        Optional<Summary> summary = summaryRepository.findByUserAndPeriodTypeAndPeriodValue(
                user,
                PeriodType.valueOf(periodType.toUpperCase()),
                periodValue);
        return summary.orElse(null);
    }

    /**
     * Cập nhật hoặc tạo mới bản ghi Summary khi có giao dịch.
     *
     * @param userId         ID của người dùng
     * @param oldTransaction Giao dịch cũ (trước khi sửa hoặc xóa, null nếu là thêm
     *                       mới)
     * @param newTransaction Giao dịch mới (sau khi thêm hoặc sửa, null nếu là xóa)
     */
    @Transactional(readOnly = false)
    public void updateSummaryForTransaction(Long userId, Transaction oldTransaction, Transaction newTransaction) {
        LOG.debug("Updating summary for userId: {}, oldTransaction: {}, newTransaction: {}", userId, oldTransaction,
                newTransaction);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        // Xử lý các kỳ cũ (dựa trên oldTransaction) để trừ giá trị
        if (oldTransaction != null) {
            LocalDate oldTransactionDate = oldTransaction.getTransactionDate().atZone(ZoneId.systemDefault())
                    .toLocalDate();
            String oldWeekPeriodValue = getPeriodValue(oldTransactionDate, "WEEK");
            String oldMonthPeriodValue = getPeriodValue(oldTransactionDate, "MONTH");
            String oldYearPeriodValue = getPeriodValue(oldTransactionDate, "YEAR");

            // Trừ giá trị từ các kỳ cũ
            updateSummaryForPeriod(user, "WEEK", oldWeekPeriodValue, oldTransaction, null);
            updateSummaryForPeriod(user, "MONTH", oldMonthPeriodValue, oldTransaction, null);
            updateSummaryForPeriod(user, "YEAR", oldYearPeriodValue, oldTransaction, null);
        }

        // Xử lý các kỳ mới (dựa trên newTransaction) để cộng giá trị
        if (newTransaction != null) {
            LocalDate newTransactionDate = newTransaction.getTransactionDate().atZone(ZoneId.systemDefault())
                    .toLocalDate();
            String newWeekPeriodValue = getPeriodValue(newTransactionDate, "WEEK");
            String newMonthPeriodValue = getPeriodValue(newTransactionDate, "MONTH");
            String newYearPeriodValue = getPeriodValue(newTransactionDate, "YEAR");

            // Cộng giá trị vào các kỳ mới
            updateSummaryForPeriod(user, "WEEK", newWeekPeriodValue, null, newTransaction);
            updateSummaryForPeriod(user, "MONTH", newMonthPeriodValue, null, newTransaction);
            updateSummaryForPeriod(user, "YEAR", newYearPeriodValue, null, newTransaction);
        }
    }

    @SuppressWarnings("deprecation")
    private void updateSummaryForPeriod(
            User user,
            String periodType,
            String periodValue,
            Transaction oldTransaction,
            Transaction newTransaction) {
        LOG.debug("Updating summary for user: {}, periodType: {}, periodValue: {}", user.getId(), periodType,
                periodValue);

        Summary summary = summaryRepository
                .findByUserAndPeriodTypeAndPeriodValue(user, PeriodType.valueOf(periodType), periodValue)
                .orElse(null);
        if (summary == null) {
            LOG.debug("Creating new summary for periodType: {}, periodValue: {}", periodType, periodValue);
            summary = new Summary();
            summary.setUser(user);
            summary.setPeriodType(PeriodType.valueOf(periodType));
            summary.setPeriodValue(periodValue);
            summary.setTotalAssets(BigDecimal.ZERO);
            summary.setTotalIncome(BigDecimal.ZERO);
            summary.setTotalExpense(BigDecimal.ZERO);
            summary.setTotalProfit(BigDecimal.ZERO);
            summary.setProfitPercentage(BigDecimal.ZERO);
        }

        // Trừ giá trị cũ (nếu có)
        if (oldTransaction != null) {
            BigDecimal amount = oldTransaction.getAmount();
            if (oldTransaction.getTransactionType() == TransactionType.INCOME) {
                summary.setTotalIncome(summary.getTotalIncome().subtract(amount));
            } else if (oldTransaction.getTransactionType() == TransactionType.EXPENSE) {
                summary.setTotalExpense(summary.getTotalExpense().subtract(amount));
            }
        }

        // Cộng giá trị mới (nếu có)
        if (newTransaction != null) {
            BigDecimal amount = newTransaction.getAmount();
            if (newTransaction.getTransactionType() == TransactionType.INCOME) {
                summary.setTotalIncome(summary.getTotalIncome().add(amount));
            } else if (newTransaction.getTransactionType() == TransactionType.EXPENSE) {
                summary.setTotalExpense(summary.getTotalExpense().add(amount));
            }
        }

        // Đảm bảo không có giá trị âm
        if (summary.getTotalIncome().compareTo(BigDecimal.ZERO) < 0) {
            summary.setTotalIncome(BigDecimal.ZERO);
        }
        if (summary.getTotalExpense().compareTo(BigDecimal.ZERO) < 0) {
            summary.setTotalExpense(BigDecimal.ZERO);
        }

        // Cập nhật totalAssets (giả định totalAssets = totalIncome - totalExpense)
        summary.setTotalAssets(summary.getTotalIncome().subtract(summary.getTotalExpense()));
        // Cập nhật totalProfit và profitPercentage
        summary.setTotalProfit(summary.getTotalIncome().subtract(summary.getTotalExpense()));
        summary.setProfitPercentage(
                summary.getTotalIncome().compareTo(BigDecimal.ZERO) != 0
                        ? summary.getTotalProfit().divide(summary.getTotalIncome(), 2, BigDecimal.ROUND_HALF_UP)
                                .multiply(BigDecimal.valueOf(100))
                        : BigDecimal.ZERO);

        // Xóa bản ghi Summary nếu không còn giao dịch (tùy chọn)
        if (summary.getTotalIncome().compareTo(BigDecimal.ZERO) == 0
                && summary.getTotalExpense().compareTo(BigDecimal.ZERO) == 0) {
            summaryRepository.delete(summary);
            LOG.debug("Deleted summary for periodType: {}, periodValue: {} as it has no transactions", periodType,
                    periodValue);
            return;
        }

        LOG.debug("Saving summary: {}", summary);
        try {
            summaryRepository.save(summary);
            LOG.debug("Successfully saved summary for periodType: {}, periodValue: {}", periodType, periodValue);
        } catch (Exception e) {
            LOG.error("Failed to save summary for periodType: {}, periodValue: {}. Error: {}", periodType, periodValue,
                    e.getMessage(), e);
            throw new RuntimeException("Failed to save summary for periodType: " + periodType, e);
        }
    }

    /**
     * Lớp lưu trữ phần trăm thay đổi của các giá trị tài chính.
     */
    public static class FinancialChange {

        private final double assetsChangePercentage;
        private final double incomeChangePercentage;
        private final double expenseChangePercentage;
        private final double profitChangePercentage;

        public FinancialChange(
                double assetsChangePercentage,
                double incomeChangePercentage,
                double expenseChangePercentage,
                double profitChangePercentage) {
            this.assetsChangePercentage = assetsChangePercentage;
            this.incomeChangePercentage = incomeChangePercentage;
            this.expenseChangePercentage = expenseChangePercentage;
            this.profitChangePercentage = profitChangePercentage;
        }

        public double getAssetsChangePercentage() {
            return assetsChangePercentage;
        }

        public double getIncomeChangePercentage() {
            return incomeChangePercentage;
        }

        public double getExpenseChangePercentage() {
            return expenseChangePercentage;
        }

        public double getProfitChangePercentage() {
            return profitChangePercentage;
        }
    }

    /**
     * Tính phần trăm thay đổi của totalAssets, totalIncome, totalExpense, và
     * profitPercentage
     * giữa kỳ hiện tại và kỳ trước đó.
     *
     * @param userId ID của người dùng
     * @param period Loại kỳ (week, month, year)
     * @return FinancialChange chứa các phần trăm thay đổi
     */
    public FinancialChange getFinancialChangeForPeriod(Long userId, String period) {
        LOG.debug("Calculating financial change for userId: {}, period: {}", userId, period);
        LocalDate now = LocalDate.now();
        String currentPeriodValue, previousPeriodValue;

        // Xác định kỳ hiện tại và kỳ trước đó
        switch (period.toLowerCase()) {
            case "week":
                currentPeriodValue = getPeriodValue(now, "WEEK");
                previousPeriodValue = getPeriodValue(now.minusWeeks(1), "WEEK");
                break;
            case "month":
                currentPeriodValue = getPeriodValue(now, "MONTH");
                previousPeriodValue = getPeriodValue(now.minusMonths(1), "MONTH");
                break;
            case "year":
                currentPeriodValue = getPeriodValue(now, "YEAR");
                previousPeriodValue = getPeriodValue(now.minusYears(1), "YEAR");
                break;
            default:
                currentPeriodValue = getPeriodValue(now, "MONTH");
                previousPeriodValue = getPeriodValue(now.minusMonths(1), "MONTH");
                break;
        }

        // Lấy bản ghi Summary cho kỳ hiện tại và kỳ trước
        Summary currentSummary = getSummaryForPeriod(userId, period, currentPeriodValue);
        Summary previousSummary = getSummaryForPeriod(userId, period, previousPeriodValue);

        // Nếu không có dữ liệu, trả về 0% cho tất cả
        if (currentSummary == null || previousSummary == null) {
            return new FinancialChange(0, 0, 0, 0);
        }

        // Tính phần trăm thay đổi
        double assetsChange = calculateChangePercentage(previousSummary.getTotalAssets(),
                currentSummary.getTotalAssets());
        double incomeChange = calculateChangePercentage(previousSummary.getTotalIncome(),
                currentSummary.getTotalIncome());
        double expenseChange = calculateChangePercentage(previousSummary.getTotalExpense(),
                currentSummary.getTotalExpense());
        double profitChange = calculateChangePercentage(previousSummary.getProfitPercentage(),
                currentSummary.getProfitPercentage());

        return new FinancialChange(assetsChange, incomeChange, expenseChange, profitChange);
    }

    /**
     * Tính phần trăm thay đổi giữa hai giá trị.
     */
    @SuppressWarnings("deprecation")
    private double calculateChangePercentage(BigDecimal previous, BigDecimal current) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }
        return current.subtract(previous).divide(previous, 2, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue();
    }

    /**
     * Tính periodValue dựa trên ngày và loại kỳ.
     */
    private String getPeriodValue(LocalDate date, String periodType) {
        switch (periodType.toUpperCase()) {
            case "WEEK":
                int weekOfYear = date.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
                return date.getYear() + "-" + String.format("%02d", weekOfYear);
            case "MONTH":
                return date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            case "YEAR":
                return String.valueOf(date.getYear());
            default:
                throw new IllegalArgumentException("Invalid period type: " + periodType);
        }
    }

    /**
     * Return a {@link Page} of {@link Summary} which matches the criteria from the
     * database.
     *
     * @param criteria The object which holds all the filters, which the entities
     *                 should match.
     * @param page     The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<Summary> findByCriteria(SummaryCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Summary> specification = createSpecification(criteria);
        return summaryRepository.findAll(specification, page);
    }

    /**
     * Return the number of matching entities in the database.
     *
     * @param criteria The object which holds all the filters, which the entities
     *                 should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(SummaryCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Summary> specification = createSpecification(criteria);
        return summaryRepository.count(specification);
    }

    /**
     * Function to convert {@link SummaryCriteria} to a {@link Specification}
     *
     * @param criteria The object which holds all the filters, which the entities
     *                 should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Summary> createSpecification(SummaryCriteria criteria) {
        Specification<Summary> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), Summary_.id));
            }
            if (criteria.getPeriodType() != null) {
                specification = specification.and(buildSpecification(criteria.getPeriodType(), Summary_.periodType));
            }
            if (criteria.getPeriodValue() != null) {
                specification = specification
                        .and(buildStringSpecification(criteria.getPeriodValue(), Summary_.periodValue));
            }
            if (criteria.getTotalAssets() != null) {
                specification = specification
                        .and(buildRangeSpecification(criteria.getTotalAssets(), Summary_.totalAssets));
            }
            if (criteria.getTotalIncome() != null) {
                specification = specification
                        .and(buildRangeSpecification(criteria.getTotalIncome(), Summary_.totalIncome));
            }
            if (criteria.getTotalExpense() != null) {
                specification = specification
                        .and(buildRangeSpecification(criteria.getTotalExpense(), Summary_.totalExpense));
            }
            if (criteria.getTotalProfit() != null) {
                specification = specification
                        .and(buildRangeSpecification(criteria.getTotalProfit(), Summary_.totalProfit));
            }
            if (criteria.getProfitPercentage() != null) {
                specification = specification
                        .and(buildRangeSpecification(criteria.getProfitPercentage(), Summary_.profitPercentage));
            }
            if (criteria.getCreatedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCreatedAt(), Summary_.createdAt));
            }
            if (criteria.getUpdatedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getUpdatedAt(), Summary_.updatedAt));
            }
            if (criteria.getUserId() != null) {
                specification = specification.and(
                        buildSpecification(criteria.getUserId(),
                                root -> root.join(Summary_.user, JoinType.LEFT).get(User_.id)));
            }
        }
        return specification;
    }
}
