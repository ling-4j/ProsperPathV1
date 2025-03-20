package com.mycompany.myapp.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class TransactionCriteriaTest {

    @Test
    void newTransactionCriteriaHasAllFiltersNullTest() {
        var transactionCriteria = new TransactionCriteria();
        assertThat(transactionCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void transactionCriteriaFluentMethodsCreatesFiltersTest() {
        var transactionCriteria = new TransactionCriteria();

        setAllFilters(transactionCriteria);

        assertThat(transactionCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void transactionCriteriaCopyCreatesNullFilterTest() {
        var transactionCriteria = new TransactionCriteria();
        var copy = transactionCriteria.copy();

        assertThat(transactionCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(transactionCriteria)
        );
    }

    @Test
    void transactionCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var transactionCriteria = new TransactionCriteria();
        setAllFilters(transactionCriteria);

        var copy = transactionCriteria.copy();

        assertThat(transactionCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(transactionCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var transactionCriteria = new TransactionCriteria();

        assertThat(transactionCriteria).hasToString("TransactionCriteria{}");
    }

    private static void setAllFilters(TransactionCriteria transactionCriteria) {
        transactionCriteria.id();
        transactionCriteria.amount();
        transactionCriteria.transactionType();
        transactionCriteria.description();
        transactionCriteria.transactionDate();
        transactionCriteria.createdAt();
        transactionCriteria.updatedAt();
        transactionCriteria.categoryId();
        transactionCriteria.userId();
        transactionCriteria.distinct();
    }

    private static Condition<TransactionCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getAmount()) &&
                condition.apply(criteria.getTransactionType()) &&
                condition.apply(criteria.getDescription()) &&
                condition.apply(criteria.getTransactionDate()) &&
                condition.apply(criteria.getCreatedAt()) &&
                condition.apply(criteria.getUpdatedAt()) &&
                condition.apply(criteria.getCategoryId()) &&
                condition.apply(criteria.getUserId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<TransactionCriteria> copyFiltersAre(TransactionCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getAmount(), copy.getAmount()) &&
                condition.apply(criteria.getTransactionType(), copy.getTransactionType()) &&
                condition.apply(criteria.getDescription(), copy.getDescription()) &&
                condition.apply(criteria.getTransactionDate(), copy.getTransactionDate()) &&
                condition.apply(criteria.getCreatedAt(), copy.getCreatedAt()) &&
                condition.apply(criteria.getUpdatedAt(), copy.getUpdatedAt()) &&
                condition.apply(criteria.getCategoryId(), copy.getCategoryId()) &&
                condition.apply(criteria.getUserId(), copy.getUserId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
