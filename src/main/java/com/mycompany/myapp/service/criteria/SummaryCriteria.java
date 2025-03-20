package com.mycompany.myapp.service.criteria;

import com.mycompany.myapp.domain.enumeration.PeriodType;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.mycompany.myapp.domain.Summary} entity. This class is used
 * in {@link com.mycompany.myapp.web.rest.SummaryResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /summaries?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class SummaryCriteria implements Serializable, Criteria {

    /**
     * Class for filtering PeriodType
     */
    public static class PeriodTypeFilter extends Filter<PeriodType> {

        public PeriodTypeFilter() {}

        public PeriodTypeFilter(PeriodTypeFilter filter) {
            super(filter);
        }

        @Override
        public PeriodTypeFilter copy() {
            return new PeriodTypeFilter(this);
        }
    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private PeriodTypeFilter periodType;

    private StringFilter periodValue;

    private BigDecimalFilter totalAssets;

    private BigDecimalFilter totalIncome;

    private BigDecimalFilter totalExpense;

    private BigDecimalFilter totalProfit;

    private BigDecimalFilter profitPercentage;

    private InstantFilter createdAt;

    private InstantFilter updatedAt;

    private LongFilter userId;

    private Boolean distinct;

    public SummaryCriteria() {}

    public SummaryCriteria(SummaryCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.periodType = other.optionalPeriodType().map(PeriodTypeFilter::copy).orElse(null);
        this.periodValue = other.optionalPeriodValue().map(StringFilter::copy).orElse(null);
        this.totalAssets = other.optionalTotalAssets().map(BigDecimalFilter::copy).orElse(null);
        this.totalIncome = other.optionalTotalIncome().map(BigDecimalFilter::copy).orElse(null);
        this.totalExpense = other.optionalTotalExpense().map(BigDecimalFilter::copy).orElse(null);
        this.totalProfit = other.optionalTotalProfit().map(BigDecimalFilter::copy).orElse(null);
        this.profitPercentage = other.optionalProfitPercentage().map(BigDecimalFilter::copy).orElse(null);
        this.createdAt = other.optionalCreatedAt().map(InstantFilter::copy).orElse(null);
        this.updatedAt = other.optionalUpdatedAt().map(InstantFilter::copy).orElse(null);
        this.userId = other.optionalUserId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public SummaryCriteria copy() {
        return new SummaryCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public Optional<LongFilter> optionalId() {
        return Optional.ofNullable(id);
    }

    public LongFilter id() {
        if (id == null) {
            setId(new LongFilter());
        }
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public PeriodTypeFilter getPeriodType() {
        return periodType;
    }

    public Optional<PeriodTypeFilter> optionalPeriodType() {
        return Optional.ofNullable(periodType);
    }

    public PeriodTypeFilter periodType() {
        if (periodType == null) {
            setPeriodType(new PeriodTypeFilter());
        }
        return periodType;
    }

    public void setPeriodType(PeriodTypeFilter periodType) {
        this.periodType = periodType;
    }

    public StringFilter getPeriodValue() {
        return periodValue;
    }

    public Optional<StringFilter> optionalPeriodValue() {
        return Optional.ofNullable(periodValue);
    }

    public StringFilter periodValue() {
        if (periodValue == null) {
            setPeriodValue(new StringFilter());
        }
        return periodValue;
    }

    public void setPeriodValue(StringFilter periodValue) {
        this.periodValue = periodValue;
    }

    public BigDecimalFilter getTotalAssets() {
        return totalAssets;
    }

    public Optional<BigDecimalFilter> optionalTotalAssets() {
        return Optional.ofNullable(totalAssets);
    }

    public BigDecimalFilter totalAssets() {
        if (totalAssets == null) {
            setTotalAssets(new BigDecimalFilter());
        }
        return totalAssets;
    }

    public void setTotalAssets(BigDecimalFilter totalAssets) {
        this.totalAssets = totalAssets;
    }

    public BigDecimalFilter getTotalIncome() {
        return totalIncome;
    }

    public Optional<BigDecimalFilter> optionalTotalIncome() {
        return Optional.ofNullable(totalIncome);
    }

    public BigDecimalFilter totalIncome() {
        if (totalIncome == null) {
            setTotalIncome(new BigDecimalFilter());
        }
        return totalIncome;
    }

    public void setTotalIncome(BigDecimalFilter totalIncome) {
        this.totalIncome = totalIncome;
    }

    public BigDecimalFilter getTotalExpense() {
        return totalExpense;
    }

    public Optional<BigDecimalFilter> optionalTotalExpense() {
        return Optional.ofNullable(totalExpense);
    }

    public BigDecimalFilter totalExpense() {
        if (totalExpense == null) {
            setTotalExpense(new BigDecimalFilter());
        }
        return totalExpense;
    }

    public void setTotalExpense(BigDecimalFilter totalExpense) {
        this.totalExpense = totalExpense;
    }

    public BigDecimalFilter getTotalProfit() {
        return totalProfit;
    }

    public Optional<BigDecimalFilter> optionalTotalProfit() {
        return Optional.ofNullable(totalProfit);
    }

    public BigDecimalFilter totalProfit() {
        if (totalProfit == null) {
            setTotalProfit(new BigDecimalFilter());
        }
        return totalProfit;
    }

    public void setTotalProfit(BigDecimalFilter totalProfit) {
        this.totalProfit = totalProfit;
    }

    public BigDecimalFilter getProfitPercentage() {
        return profitPercentage;
    }

    public Optional<BigDecimalFilter> optionalProfitPercentage() {
        return Optional.ofNullable(profitPercentage);
    }

    public BigDecimalFilter profitPercentage() {
        if (profitPercentage == null) {
            setProfitPercentage(new BigDecimalFilter());
        }
        return profitPercentage;
    }

    public void setProfitPercentage(BigDecimalFilter profitPercentage) {
        this.profitPercentage = profitPercentage;
    }

    public InstantFilter getCreatedAt() {
        return createdAt;
    }

    public Optional<InstantFilter> optionalCreatedAt() {
        return Optional.ofNullable(createdAt);
    }

    public InstantFilter createdAt() {
        if (createdAt == null) {
            setCreatedAt(new InstantFilter());
        }
        return createdAt;
    }

    public void setCreatedAt(InstantFilter createdAt) {
        this.createdAt = createdAt;
    }

    public InstantFilter getUpdatedAt() {
        return updatedAt;
    }

    public Optional<InstantFilter> optionalUpdatedAt() {
        return Optional.ofNullable(updatedAt);
    }

    public InstantFilter updatedAt() {
        if (updatedAt == null) {
            setUpdatedAt(new InstantFilter());
        }
        return updatedAt;
    }

    public void setUpdatedAt(InstantFilter updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LongFilter getUserId() {
        return userId;
    }

    public Optional<LongFilter> optionalUserId() {
        return Optional.ofNullable(userId);
    }

    public LongFilter userId() {
        if (userId == null) {
            setUserId(new LongFilter());
        }
        return userId;
    }

    public void setUserId(LongFilter userId) {
        this.userId = userId;
    }

    public Boolean getDistinct() {
        return distinct;
    }

    public Optional<Boolean> optionalDistinct() {
        return Optional.ofNullable(distinct);
    }

    public Boolean distinct() {
        if (distinct == null) {
            setDistinct(true);
        }
        return distinct;
    }

    public void setDistinct(Boolean distinct) {
        this.distinct = distinct;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final SummaryCriteria that = (SummaryCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(periodType, that.periodType) &&
            Objects.equals(periodValue, that.periodValue) &&
            Objects.equals(totalAssets, that.totalAssets) &&
            Objects.equals(totalIncome, that.totalIncome) &&
            Objects.equals(totalExpense, that.totalExpense) &&
            Objects.equals(totalProfit, that.totalProfit) &&
            Objects.equals(profitPercentage, that.profitPercentage) &&
            Objects.equals(createdAt, that.createdAt) &&
            Objects.equals(updatedAt, that.updatedAt) &&
            Objects.equals(userId, that.userId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            periodType,
            periodValue,
            totalAssets,
            totalIncome,
            totalExpense,
            totalProfit,
            profitPercentage,
            createdAt,
            updatedAt,
            userId,
            distinct
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "SummaryCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalPeriodType().map(f -> "periodType=" + f + ", ").orElse("") +
            optionalPeriodValue().map(f -> "periodValue=" + f + ", ").orElse("") +
            optionalTotalAssets().map(f -> "totalAssets=" + f + ", ").orElse("") +
            optionalTotalIncome().map(f -> "totalIncome=" + f + ", ").orElse("") +
            optionalTotalExpense().map(f -> "totalExpense=" + f + ", ").orElse("") +
            optionalTotalProfit().map(f -> "totalProfit=" + f + ", ").orElse("") +
            optionalProfitPercentage().map(f -> "profitPercentage=" + f + ", ").orElse("") +
            optionalCreatedAt().map(f -> "createdAt=" + f + ", ").orElse("") +
            optionalUpdatedAt().map(f -> "updatedAt=" + f + ", ").orElse("") +
            optionalUserId().map(f -> "userId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
