package com.mycompany.myapp.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class CategoryCriteriaTest {

    @Test
    void newCategoryCriteriaHasAllFiltersNullTest() {
        var categoryCriteria = new CategoryCriteria();
        assertThat(categoryCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void categoryCriteriaFluentMethodsCreatesFiltersTest() {
        var categoryCriteria = new CategoryCriteria();

        setAllFilters(categoryCriteria);

        assertThat(categoryCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void categoryCriteriaCopyCreatesNullFilterTest() {
        var categoryCriteria = new CategoryCriteria();
        var copy = categoryCriteria.copy();

        assertThat(categoryCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(categoryCriteria)
        );
    }

    @Test
    void categoryCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var categoryCriteria = new CategoryCriteria();
        setAllFilters(categoryCriteria);

        var copy = categoryCriteria.copy();

        assertThat(categoryCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(categoryCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var categoryCriteria = new CategoryCriteria();

        assertThat(categoryCriteria).hasToString("CategoryCriteria{}");
    }

    private static void setAllFilters(CategoryCriteria categoryCriteria) {
        categoryCriteria.id();
        categoryCriteria.categoryName();
        categoryCriteria.categoryType();
        categoryCriteria.createdAt();
        categoryCriteria.updatedAt();
        categoryCriteria.distinct();
    }

    private static Condition<CategoryCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getCategoryName()) &&
                condition.apply(criteria.getCategoryType()) &&
                condition.apply(criteria.getCreatedAt()) &&
                condition.apply(criteria.getUpdatedAt()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<CategoryCriteria> copyFiltersAre(CategoryCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getCategoryName(), copy.getCategoryName()) &&
                condition.apply(criteria.getCategoryType(), copy.getCategoryType()) &&
                condition.apply(criteria.getCreatedAt(), copy.getCreatedAt()) &&
                condition.apply(criteria.getUpdatedAt(), copy.getUpdatedAt()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
