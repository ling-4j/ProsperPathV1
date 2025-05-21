package com.mycompany.myapp.service.impl;

import com.mycompany.myapp.domain.Summary;
import com.mycompany.myapp.domain.Transaction;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.domain.enumeration.PeriodType;
import com.mycompany.myapp.domain.enumeration.TransactionType;
import com.mycompany.myapp.repository.SummaryRepository;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.service.SummaryService;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing
 * {@link com.mycompany.myapp.domain.Summary}.
 */
@Service
@Transactional
public class SummaryServiceImpl implements SummaryService {

    private static final Logger LOG = LoggerFactory.getLogger(SummaryServiceImpl.class);

    private final SummaryRepository summaryRepository;
    private final UserRepository userRepository;

    public SummaryServiceImpl(SummaryRepository summaryRepository, UserRepository userRepository) {
        this.summaryRepository = summaryRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Summary save(Summary summary) {
        LOG.debug("Request to save Summary : {}", summary);
        return summaryRepository.save(summary);
    }

    @Override
    public Summary update(Summary summary) {
        LOG.debug("Request to update Summary : {}", summary);
        return summaryRepository.save(summary);
    }

    @Override
    public Optional<Summary> partialUpdate(Summary summary) {
        LOG.debug("Request to partially update Summary : {}", summary);

        return summaryRepository
            .findById(summary.getId())
            .map(existingSummary -> {
                if (summary.getPeriodType() != null) {
                    existingSummary.setPeriodType(summary.getPeriodType());
                }
                if (summary.getPeriodValue() != null) {
                    existingSummary.setPeriodValue(summary.getPeriodValue());
                }
                if (summary.getTotalAssets() != null) {
                    existingSummary.setTotalAssets(summary.getTotalAssets());
                }
                if (summary.getTotalIncome() != null) {
                    existingSummary.setTotalIncome(summary.getTotalIncome());
                }
                if (summary.getTotalExpense() != null) {
                    existingSummary.setTotalExpense(summary.getTotalExpense());
                }
                if (summary.getTotalProfit() != null) {
                    existingSummary.setTotalProfit(summary.getTotalProfit());
                }
                if (summary.getProfitPercentage() != null) {
                    existingSummary.setProfitPercentage(summary.getProfitPercentage());
                }
                if (summary.getCreatedAt() != null) {
                    existingSummary.setCreatedAt(summary.getCreatedAt());
                }
                if (summary.getUpdatedAt() != null) {
                    existingSummary.setUpdatedAt(summary.getUpdatedAt());
                }

                return existingSummary;
            })
            .map(summaryRepository::save);
    }

    public Page<Summary> findAllWithEagerRelationships(Pageable pageable) {
        return summaryRepository.findAllWithEagerRelationships(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Summary> findOne(Long id) {
        LOG.debug("Request to get Summary : {}", id);
        return summaryRepository.findOneWithEagerRelationships(id);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Summary : {}", id);
        summaryRepository.deleteById(id);
    }

    @Override
    public void updateSummaryForTransaction(Long userId, Transaction oldTransaction, Transaction newTransaction) {
        LOG.debug("Updating summary for userId: {}, oldTransaction: {}, newTransaction: {}", userId, oldTransaction, newTransaction);
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        // Xử lý các kỳ cũ (dựa trên oldTransaction) để trừ giá trị
        if (oldTransaction != null) {
            LocalDate oldTransactionDate = oldTransaction.getTransactionDate().atZone(ZoneId.systemDefault()).toLocalDate();
            String oldWeekPeriodValue = getPeriodValue(oldTransactionDate, "WEEK");
            String oldMonthPeriodValue = getPeriodValue(oldTransactionDate, "MONTH");
            String oldYearPeriodValue = getPeriodValue(oldTransactionDate, "YEAR");

            updateSummaryForPeriod(user, "WEEK", oldWeekPeriodValue, oldTransaction, null);
            updateSummaryForPeriod(user, "MONTH", oldMonthPeriodValue, oldTransaction, null);
            updateSummaryForPeriod(user, "YEAR", oldYearPeriodValue, oldTransaction, null);
        }

        // Xử lý các kỳ mới (dựa trên newTransaction) để cộng giá trị
        if (newTransaction != null) {
            LocalDate newTransactionDate = newTransaction.getTransactionDate().atZone(ZoneId.systemDefault()).toLocalDate();
            String newWeekPeriodValue = getPeriodValue(newTransactionDate, "WEEK");
            String newMonthPeriodValue = getPeriodValue(newTransactionDate, "MONTH");
            String newYearPeriodValue = getPeriodValue(newTransactionDate, "YEAR");

            updateSummaryForPeriod(user, "WEEK", newWeekPeriodValue, null, newTransaction);
            updateSummaryForPeriod(user, "MONTH", newMonthPeriodValue, null, newTransaction);
            updateSummaryForPeriod(user, "YEAR", newYearPeriodValue, null, newTransaction);
        }
    }

    private void updateSummaryForPeriod(
        User user,
        String periodType,
        String periodValue,
        Transaction oldTransaction,
        Transaction newTransaction
    ) {
        LOG.debug("Updating summary for user: {}, periodType: {}, periodValue: {}", user.getId(), periodType, periodValue);

        Summary summary = summaryRepository
            .findByUserAndPeriodTypeAndPeriodValue(user, PeriodType.valueOf(periodType), periodValue)
            .orElse(null);
        if (summary == null) {
            LOG.debug("Creating new summary for periodType: {}, periodValue: {}", periodType, periodValue);
            summary = createEmptySummary(user, periodType, periodValue);
        }

        // Trừ giá trị cũ (nếu có)
        if (oldTransaction != null) {
            adjustSummary(summary, oldTransaction, false);
        }

        // Cộng giá trị mới (nếu có)
        if (newTransaction != null) {
            adjustSummary(summary, newTransaction, true);
        }

        updateSummaryValues(summary);

        if (shouldDeleteSummary(summary)) {
            summaryRepository.delete(summary);
            LOG.debug("Deleted summary for periodType: {}, periodValue: {} as it has no transactions", periodType, periodValue);
            return;
        }

        LOG.debug("Saving summary: {}", summary);
        try {
            summaryRepository.save(summary);
            LOG.debug("Successfully saved summary for periodType: {}, periodValue: {}", periodType, periodValue);
        } catch (Exception e) {
            LOG.error("Failed to save summary for periodType: {}, periodValue: {}. Error: {}", periodType, periodValue, e.getMessage(), e);
            throw new RuntimeException("Failed to save summary for periodType: " + periodType, e);
        }
    }

    private Summary createEmptySummary(User user, String periodType, String periodValue) {
        Summary summary = new Summary();
        summary.setUser(user);
        summary.setPeriodType(PeriodType.valueOf(periodType));
        summary.setPeriodValue(periodValue);
        summary.setTotalAssets(BigDecimal.ZERO);
        summary.setTotalIncome(BigDecimal.ZERO);
        summary.setTotalExpense(BigDecimal.ZERO);
        summary.setTotalProfit(BigDecimal.ZERO);
        summary.setProfitPercentage(BigDecimal.ZERO);
        return summary;
    }

    private void adjustSummary(Summary summary, Transaction transaction, boolean isAdd) {
        BigDecimal amount = transaction.getAmount();
        if (transaction.getTransactionType() == TransactionType.INCOME) {
            summary.setTotalIncome(isAdd ? summary.getTotalIncome().add(amount) : summary.getTotalIncome().subtract(amount));
        } else if (transaction.getTransactionType() == TransactionType.EXPENSE) {
            summary.setTotalExpense(isAdd ? summary.getTotalExpense().add(amount) : summary.getTotalExpense().subtract(amount));
        }
    }

    @SuppressWarnings("deprecation")
    private void updateSummaryValues(Summary summary) {
        if (summary.getTotalIncome().compareTo(BigDecimal.ZERO) < 0) {
            summary.setTotalIncome(BigDecimal.ZERO);
        }
        if (summary.getTotalExpense().compareTo(BigDecimal.ZERO) < 0) {
            summary.setTotalExpense(BigDecimal.ZERO);
        }
        summary.setTotalAssets(summary.getTotalIncome().subtract(summary.getTotalExpense()));
        summary.setTotalProfit(summary.getTotalIncome().subtract(summary.getTotalExpense()));
        summary.setProfitPercentage(
            summary.getTotalIncome().compareTo(BigDecimal.ZERO) != 0
                ? summary.getTotalProfit().divide(summary.getTotalIncome(), 2, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO
        );
    }

    private boolean shouldDeleteSummary(Summary summary) {
        return summary.getTotalIncome().compareTo(BigDecimal.ZERO) == 0 && summary.getTotalExpense().compareTo(BigDecimal.ZERO) == 0;
    }

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
}
