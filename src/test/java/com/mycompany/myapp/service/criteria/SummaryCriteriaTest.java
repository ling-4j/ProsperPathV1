package com.mycompany.myapp.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class SummaryCriteriaTest {

    @Test
    void newSummaryCriteriaHasAllFiltersNullTest() {
        var summaryCriteria = new SummaryCriteria();
        assertThat(summaryCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void summaryCriteriaFluentMethodsCreatesFiltersTest() {
        var summaryCriteria = new SummaryCriteria();

        setAllFilters(summaryCriteria);

        assertThat(summaryCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void summaryCriteriaCopyCreatesNullFilterTest() {
        var summaryCriteria = new SummaryCriteria();
        var copy = summaryCriteria.copy();

        assertThat(summaryCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(summaryCriteria)
        );
    }

    @Test
    void summaryCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var summaryCriteria = new SummaryCriteria();
        setAllFilters(summaryCriteria);

        var copy = summaryCriteria.copy();

        assertThat(summaryCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(summaryCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var summaryCriteria = new SummaryCriteria();

        assertThat(summaryCriteria).hasToString("SummaryCriteria{}");
    }

    private static void setAllFilters(SummaryCriteria summaryCriteria) {
        summaryCriteria.id();
        summaryCriteria.periodType();
        summaryCriteria.periodValue();
        summaryCriteria.totalAssets();
        summaryCriteria.totalIncome();
        summaryCriteria.totalExpense();
        summaryCriteria.totalProfit();
        summaryCriteria.profitPercentage();
        summaryCriteria.createdAt();
        summaryCriteria.updatedAt();
        summaryCriteria.userId();
        summaryCriteria.distinct();
    }

    private static Condition<SummaryCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getPeriodType()) &&
                condition.apply(criteria.getPeriodValue()) &&
                condition.apply(criteria.getTotalAssets()) &&
                condition.apply(criteria.getTotalIncome()) &&
                condition.apply(criteria.getTotalExpense()) &&
                condition.apply(criteria.getTotalProfit()) &&
                condition.apply(criteria.getProfitPercentage()) &&
                condition.apply(criteria.getCreatedAt()) &&
                condition.apply(criteria.getUpdatedAt()) &&
                condition.apply(criteria.getUserId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<SummaryCriteria> copyFiltersAre(SummaryCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getPeriodType(), copy.getPeriodType()) &&
                condition.apply(criteria.getPeriodValue(), copy.getPeriodValue()) &&
                condition.apply(criteria.getTotalAssets(), copy.getTotalAssets()) &&
                condition.apply(criteria.getTotalIncome(), copy.getTotalIncome()) &&
                condition.apply(criteria.getTotalExpense(), copy.getTotalExpense()) &&
                condition.apply(criteria.getTotalProfit(), copy.getTotalProfit()) &&
                condition.apply(criteria.getProfitPercentage(), copy.getProfitPercentage()) &&
                condition.apply(criteria.getCreatedAt(), copy.getCreatedAt()) &&
                condition.apply(criteria.getUpdatedAt(), copy.getUpdatedAt()) &&
                condition.apply(criteria.getUserId(), copy.getUserId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
