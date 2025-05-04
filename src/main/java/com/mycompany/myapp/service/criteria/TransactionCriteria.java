package com.mycompany.myapp.service.criteria;

import com.mycompany.myapp.domain.enumeration.TransactionType;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.mycompany.myapp.domain.Transaction} entity. This class is used
 * in {@link com.mycompany.myapp.web.rest.TransactionResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /transactions?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class TransactionCriteria implements Serializable, Criteria {

    /**
     * Class for filtering TransactionType
     */
    public static class TransactionTypeFilter extends Filter<TransactionType> {

        public TransactionTypeFilter() {}

        public TransactionTypeFilter(TransactionTypeFilter filter) {
            super(filter);
        }

        @Override
        public TransactionTypeFilter copy() {
            return new TransactionTypeFilter(this);
        }
    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private BigDecimalFilter amount;

    private TransactionTypeFilter transactionType;

    private StringFilter description;

    private InstantFilter transactionDate;

    private InstantFilter createdAt;

    private InstantFilter updatedAt;

    private LongFilter categoryId;

    private LongFilter userId;

    private Boolean distinct;

    private LongFilter category;

    private InstantFilter fromDate;

    private InstantFilter toDate;

    private TransactionTypeFilter type;

    public TransactionCriteria() {}

    public TransactionCriteria(TransactionCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.amount = other.optionalAmount().map(BigDecimalFilter::copy).orElse(null);
        this.transactionType = other.optionalTransactionType().map(TransactionTypeFilter::copy).orElse(null);
        this.description = other.optionalDescription().map(StringFilter::copy).orElse(null);
        this.transactionDate = other.optionalTransactionDate().map(InstantFilter::copy).orElse(null);
        this.createdAt = other.optionalCreatedAt().map(InstantFilter::copy).orElse(null);
        this.updatedAt = other.optionalUpdatedAt().map(InstantFilter::copy).orElse(null);
        this.categoryId = other.optionalCategoryId().map(LongFilter::copy).orElse(null);
        this.userId = other.optionalUserId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
        this.category = other.optionalCategory().map(LongFilter::copy).orElse(null);
        this.fromDate = other.optionalFromDate().map(InstantFilter::copy).orElse(null);
        this.toDate = other.optionalToDate().map(InstantFilter::copy).orElse(null);
        this.type = other.optionalType().map(TransactionTypeFilter::copy).orElse(null);
    }

    @Override
    public TransactionCriteria copy() {
        return new TransactionCriteria(this);
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

    public BigDecimalFilter getAmount() {
        return amount;
    }

    public Optional<BigDecimalFilter> optionalAmount() {
        return Optional.ofNullable(amount);
    }

    public BigDecimalFilter amount() {
        if (amount == null) {
            setAmount(new BigDecimalFilter());
        }
        return amount;
    }

    public void setAmount(BigDecimalFilter amount) {
        this.amount = amount;
    }

    public TransactionTypeFilter getTransactionType() {
        return transactionType;
    }

    public Optional<TransactionTypeFilter> optionalTransactionType() {
        return Optional.ofNullable(transactionType);
    }

    public TransactionTypeFilter transactionType() {
        if (transactionType == null) {
            setTransactionType(new TransactionTypeFilter());
        }
        return transactionType;
    }

    public void setTransactionType(TransactionTypeFilter transactionType) {
        this.transactionType = transactionType;
    }

    public StringFilter getDescription() {
        return description;
    }

    public Optional<StringFilter> optionalDescription() {
        return Optional.ofNullable(description);
    }

    public StringFilter description() {
        if (description == null) {
            setDescription(new StringFilter());
        }
        return description;
    }

    public void setDescription(StringFilter description) {
        this.description = description;
    }

    public InstantFilter getTransactionDate() {
        return transactionDate;
    }

    public Optional<InstantFilter> optionalTransactionDate() {
        return Optional.ofNullable(transactionDate);
    }

    public InstantFilter transactionDate() {
        if (transactionDate == null) {
            setTransactionDate(new InstantFilter());
        }
        return transactionDate;
    }

    public void setTransactionDate(InstantFilter transactionDate) {
        this.transactionDate = transactionDate;
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

    public LongFilter getCategoryId() {
        return categoryId;
    }

    public Optional<LongFilter> optionalCategoryId() {
        return Optional.ofNullable(categoryId);
    }

    public LongFilter categoryId() {
        if (categoryId == null) {
            setCategoryId(new LongFilter());
        }
        return categoryId;
    }

    public void setCategoryId(LongFilter categoryId) {
        this.categoryId = categoryId;
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

    public LongFilter getCategory() {
        return category;
    }

    public Optional<LongFilter> optionalCategory() {
        return Optional.ofNullable(category);
    }

    public LongFilter category() {
        if (category == null) {
            setCategory(new LongFilter());
        }
        return category;
    }

    public void setCategory(LongFilter category) {
        this.category = category;
    }

    public InstantFilter getFromDate() {
        return fromDate;
    }

    public Optional<InstantFilter> optionalFromDate() {
        return Optional.ofNullable(fromDate);
    }

    public InstantFilter fromDate() {
        if (fromDate == null) {
            setFromDate(new InstantFilter());
        }
        return fromDate;
    }

    public void setFromDate(InstantFilter fromDate) {
        this.fromDate = fromDate;
    }

    public InstantFilter getToDate() {
        return toDate;
    }

    public Optional<InstantFilter> optionalToDate() {
        return Optional.ofNullable(toDate);
    }

    public InstantFilter toDate() {
        if (toDate == null) {
            setToDate(new InstantFilter());
        }
        return toDate;
    }

    public void setToDate(InstantFilter toDate) {
        this.toDate = toDate;
    }

    public TransactionTypeFilter getType() {
        return type;
    }

    public Optional<TransactionTypeFilter> optionalType() {
        return Optional.ofNullable(type);
    }

    public TransactionTypeFilter type() {
        if (type == null) {
            setType(new TransactionTypeFilter());
        }
        return type;
    }

    public void setType(TransactionTypeFilter type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final TransactionCriteria that = (TransactionCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(amount, that.amount) &&
            Objects.equals(transactionType, that.transactionType) &&
            Objects.equals(description, that.description) &&
            Objects.equals(transactionDate, that.transactionDate) &&
            Objects.equals(createdAt, that.createdAt) &&
            Objects.equals(updatedAt, that.updatedAt) &&
            Objects.equals(categoryId, that.categoryId) &&
            Objects.equals(userId, that.userId) &&
            Objects.equals(distinct, that.distinct) &&
            Objects.equals(category, that.category) &&
            Objects.equals(fromDate, that.fromDate) &&
            Objects.equals(toDate, that.toDate) &&
            Objects.equals(type, that.type)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, amount, transactionType, description, transactionDate, createdAt, updatedAt, categoryId, userId, distinct, category, fromDate, toDate, type);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TransactionCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalAmount().map(f -> "amount=" + f + ", ").orElse("") +
            optionalTransactionType().map(f -> "transactionType=" + f + ", ").orElse("") +
            optionalDescription().map(f -> "description=" + f + ", ").orElse("") +
            optionalTransactionDate().map(f -> "transactionDate=" + f + ", ").orElse("") +
            optionalCreatedAt().map(f -> "createdAt=" + f + ", ").orElse("") +
            optionalUpdatedAt().map(f -> "updatedAt=" + f + ", ").orElse("") +
            optionalCategoryId().map(f -> "categoryId=" + f + ", ").orElse("") +
            optionalUserId().map(f -> "userId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
            optionalCategory().map(f -> "category=" + f + ", ").orElse("") +
            optionalFromDate().map(f -> "fromDate=" + f + ", ").orElse("") +
            optionalToDate().map(f -> "toDate=" + f + ", ").orElse("") +
            optionalType().map(f -> "type=" + f + ", ").orElse("") +
        "}";
    }
}
