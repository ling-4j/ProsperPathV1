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
    private final TransactionRepository transactionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public SummaryQueryService(
        SummaryRepository summaryRepository,
        UserRepository userRepository,
        TransactionRepository transactionRepository
    ) {
        this.summaryRepository = summaryRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
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
        LocalDate now = LocalDate.now(ZoneId.of("UTC+7"));
        LocalDate startDate;
        LocalDate endDate;

        switch (period.toUpperCase()) {
            case "WEEK":
                startDate = now.with(WeekFields.ISO.dayOfWeek(), 1);
                endDate = startDate.plusDays(6);
                break;
            case "MONTH":
                startDate = now.withDayOfMonth(1);
                endDate = now.withDayOfMonth(now.lengthOfMonth());
                break;
            case "YEAR":
                startDate = now.withMonth(1).withDayOfMonth(1); // Ngày 1/1 của năm hiện tại
                endDate = now.withMonth(12).withDayOfMonth(31); // Ngày 31/12 của năm hiện tại
                break;
            default:
                throw new IllegalArgumentException("Invalid period: " + period);
        }

        Instant startInstant = startDate.atStartOfDay(ZoneId.of("UTC+7")).toInstant();
        Instant endInstant = endDate.atTime(23, 59, 59).atZone(ZoneId.of("UTC+7")).toInstant();

        List<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateBetween(userId, startInstant, endInstant);
        LOG.info(
            "Transactions found for userId: {}, period: {}, from: {}, to: {}, count: {}",
            userId,
            period,
            startInstant,
            endInstant,
            transactions.size()
        );

        List<String> labels = new ArrayList<>();
        List<BigDecimal> incomeData = new ArrayList<>();
        List<BigDecimal> expenseData = new ArrayList<>();
        List<BigDecimal> progressRateData = new ArrayList<>();

        if (period.equalsIgnoreCase("WEEK")) {
            processWeeklyData(startDate, transactions, labels, incomeData, expenseData, progressRateData);
        } else if (period.equalsIgnoreCase("MONTH")) {
            processMonthlyData(startDate, endDate, transactions, labels, incomeData, expenseData, progressRateData);
        } else if (period.equalsIgnoreCase("YEAR")) {
            processYearlyData(startDate, endDate, transactions, labels, incomeData, expenseData, progressRateData);
        }

        LOG.debug(
            "Chart data for userId: {}, period: {}: labels={}, incomeData={}, expenseData={}, progressRateData=",
            userId,
            period,
            labels,
            incomeData,
            expenseData,
            progressRateData
        );

        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("incomeData", incomeData);
        result.put("expenseData", expenseData);
        result.put("progressRateData", progressRateData);
        return result;
    }

    private void processWeeklyData(
        LocalDate startDate,
        List<Transaction> transactions,
        List<String> labels,
        List<BigDecimal> incomeData,
        List<BigDecimal> expenseData,
        List<BigDecimal> progressRateData
    ) {
        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            labels.add(date.format(DateTimeFormatter.ofPattern("dd/MM")));

            BigDecimal income = BigDecimal.ZERO;
            BigDecimal expense = BigDecimal.ZERO;
            for (Transaction transaction : transactions) {
                LocalDate transactionDate = transaction.getTransactionDate().atZone(ZoneId.of("UTC+7")).toLocalDate();
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
    }

    private void processMonthlyData(
        LocalDate startDate,
        LocalDate endDate,
        List<Transaction> transactions,
        List<String> labels,
        List<BigDecimal> incomeData,
        List<BigDecimal> expenseData,
        List<BigDecimal> progressRateData
    ) {
        LocalDate monthStart = startDate; // Đã là ngày 1 của tháng
        LocalDate monthEnd = endDate; // Đã là ngày cuối của tháng

        int daysInMonth = monthEnd.getDayOfMonth();
        for (int i = 1; i <= daysInMonth; i += 5) {
            LocalDate startRange = monthStart.withDayOfMonth(i);
            LocalDate endRange = monthStart.withDayOfMonth(Math.min(i + 4, daysInMonth));
            labels.add(
                startRange.format(DateTimeFormatter.ofPattern("dd/MM")) + "-" + endRange.format(DateTimeFormatter.ofPattern("dd/MM"))
            );

            BigDecimal income = BigDecimal.ZERO;
            BigDecimal expense = BigDecimal.ZERO;
            for (Transaction transaction : transactions) {
                LocalDate transactionDate = transaction.getTransactionDate().atZone(ZoneId.of("UTC+7")).toLocalDate();
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
    }

    private void processYearlyData(
        LocalDate startDate,
        LocalDate endDate,
        List<Transaction> transactions,
        List<String> labels,
        List<BigDecimal> incomeData,
        List<BigDecimal> expenseData,
        List<BigDecimal> progressRateData
    ) {
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            labels.add(current.format(DateTimeFormatter.ofPattern("yyyy-MM")));

            BigDecimal income = BigDecimal.ZERO;
            BigDecimal expense = BigDecimal.ZERO;
            for (Transaction transaction : transactions) {
                LocalDate transactionDate = transaction.getTransactionDate().atZone(ZoneId.of("UTC+7")).toLocalDate();
                if (transactionDate.getYear() == current.getYear() && transactionDate.getMonthValue() == current.getMonthValue()) {
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
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        Optional<Summary> summary = summaryRepository.findByUserAndPeriodTypeAndPeriodValue(
            user,
            PeriodType.valueOf(periodType.toUpperCase()),
            periodValue
        );
        return summary.orElse(null);
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
            double profitChangePercentage
        ) {
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
        if (currentSummary == null) {
            return new FinancialChange(0, 0, 0, 0);
        }

        // Nếu không có dữ liệu kỳ trước, giả định tất cả giá trị bằng 0
        if (previousSummary == null) {
            previousSummary = new Summary();
            previousSummary.setTotalAssets(BigDecimal.ZERO);
            previousSummary.setTotalIncome(BigDecimal.ZERO);
            previousSummary.setTotalExpense(BigDecimal.ZERO);
            previousSummary.setTotalProfit(BigDecimal.ZERO);
            previousSummary.setProfitPercentage(BigDecimal.ZERO);
        }

        // Tính phần trăm thay đổi
        double assetsChange = calculateChangePercentage(previousSummary.getTotalAssets(), currentSummary.getTotalAssets());
        double incomeChange = calculateChangePercentage(previousSummary.getTotalIncome(), currentSummary.getTotalIncome());
        double expenseChange = calculateChangePercentage(previousSummary.getTotalExpense(), currentSummary.getTotalExpense());
        double profitChange = calculateChangePercentage(previousSummary.getProfitPercentage(), currentSummary.getProfitPercentage());

        return new FinancialChange(assetsChange, incomeChange, expenseChange, profitChange);
    }

    /**
     * Tính phần trăm thay đổi giữa hai giá trị.
     */
    @SuppressWarnings("deprecation")
    private double calculateChangePercentage(BigDecimal previous, BigDecimal current) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            if (current.compareTo(BigDecimal.ZERO) > 0) {
                return 100.0; // Tăng từ 0 lên giá trị dương
            } else if (current.compareTo(BigDecimal.ZERO) < 0) {
                return -100.0; // Giảm từ 0 xuống giá trị âm
            }
            return 0.0;
        }
        return current.subtract(previous).divide(previous, 2, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue();
    }

    /**
     * Tính periodValue dựa trên ngày và loại kỳ.
     */
    private String getPeriodValue(LocalDate date, String periodType) {
        switch (periodType.toUpperCase()) {
            case "WEEK":
                int weekOfYear = date.get(WeekFields.ISO.weekOfWeekBasedYear());
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
                specification = specification.and(buildStringSpecification(criteria.getPeriodValue(), Summary_.periodValue));
            }
            if (criteria.getTotalAssets() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getTotalAssets(), Summary_.totalAssets));
            }
            if (criteria.getTotalIncome() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getTotalIncome(), Summary_.totalIncome));
            }
            if (criteria.getTotalExpense() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getTotalExpense(), Summary_.totalExpense));
            }
            if (criteria.getTotalProfit() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getTotalProfit(), Summary_.totalProfit));
            }
            if (criteria.getProfitPercentage() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getProfitPercentage(), Summary_.profitPercentage));
            }
            if (criteria.getCreatedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCreatedAt(), Summary_.createdAt));
            }
            if (criteria.getUpdatedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getUpdatedAt(), Summary_.updatedAt));
            }
            if (criteria.getUserId() != null) {
                specification = specification.and(
                    buildSpecification(criteria.getUserId(), root -> root.join(Summary_.user, JoinType.LEFT).get(User_.id))
                );
            }
        }
        return specification;
    }
}
