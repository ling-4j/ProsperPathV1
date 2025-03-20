package com.mycompany.myapp.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class BudgetCriteriaTest {

    @Test
    void newBudgetCriteriaHasAllFiltersNullTest() {
        var budgetCriteria = new BudgetCriteria();
        assertThat(budgetCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void budgetCriteriaFluentMethodsCreatesFiltersTest() {
        var budgetCriteria = new BudgetCriteria();

        setAllFilters(budgetCriteria);

        assertThat(budgetCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void budgetCriteriaCopyCreatesNullFilterTest() {
        var budgetCriteria = new BudgetCriteria();
        var copy = budgetCriteria.copy();

        assertThat(budgetCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(budgetCriteria)
        );
    }

    @Test
    void budgetCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var budgetCriteria = new BudgetCriteria();
        setAllFilters(budgetCriteria);

        var copy = budgetCriteria.copy();

        assertThat(budgetCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(budgetCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var budgetCriteria = new BudgetCriteria();

        assertThat(budgetCriteria).hasToString("BudgetCriteria{}");
    }

    private static void setAllFilters(BudgetCriteria budgetCriteria) {
        budgetCriteria.id();
        budgetCriteria.budgetAmount();
        budgetCriteria.startDate();
        budgetCriteria.endDate();
        budgetCriteria.createdAt();
        budgetCriteria.updatedAt();
        budgetCriteria.categoryId();
        budgetCriteria.userId();
        budgetCriteria.distinct();
    }

    private static Condition<BudgetCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getBudgetAmount()) &&
                condition.apply(criteria.getStartDate()) &&
                condition.apply(criteria.getEndDate()) &&
                condition.apply(criteria.getCreatedAt()) &&
                condition.apply(criteria.getUpdatedAt()) &&
                condition.apply(criteria.getCategoryId()) &&
                condition.apply(criteria.getUserId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<BudgetCriteria> copyFiltersAre(BudgetCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getBudgetAmount(), copy.getBudgetAmount()) &&
                condition.apply(criteria.getStartDate(), copy.getStartDate()) &&
                condition.apply(criteria.getEndDate(), copy.getEndDate()) &&
                condition.apply(criteria.getCreatedAt(), copy.getCreatedAt()) &&
                condition.apply(criteria.getUpdatedAt(), copy.getUpdatedAt()) &&
                condition.apply(criteria.getCategoryId(), copy.getCategoryId()) &&
                condition.apply(criteria.getUserId(), copy.getUserId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
