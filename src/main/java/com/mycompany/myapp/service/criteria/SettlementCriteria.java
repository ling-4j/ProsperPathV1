package com.mycompany.myapp.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.mycompany.myapp.domain.Settlement} entity. This class is used
 * in {@link com.mycompany.myapp.web.rest.SettlementResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /settlements?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class SettlementCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private BigDecimalFilter amount;

    private LongFilter eventId;

    private LongFilter fromMemberId;

    private LongFilter toMemberId;

    private Boolean distinct;

    public SettlementCriteria() {}

    public SettlementCriteria(SettlementCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.amount = other.optionalAmount().map(BigDecimalFilter::copy).orElse(null);
        this.eventId = other.optionalEventId().map(LongFilter::copy).orElse(null);
        this.fromMemberId = other.optionalFromMemberId().map(LongFilter::copy).orElse(null);
        this.toMemberId = other.optionalToMemberId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public SettlementCriteria copy() {
        return new SettlementCriteria(this);
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

    public LongFilter getEventId() {
        return eventId;
    }

    public Optional<LongFilter> optionalEventId() {
        return Optional.ofNullable(eventId);
    }

    public LongFilter eventId() {
        if (eventId == null) {
            setEventId(new LongFilter());
        }
        return eventId;
    }

    public void setEventId(LongFilter eventId) {
        this.eventId = eventId;
    }

    public LongFilter getFromMemberId() {
        return fromMemberId;
    }

    public Optional<LongFilter> optionalFromMemberId() {
        return Optional.ofNullable(fromMemberId);
    }

    public LongFilter fromMemberId() {
        if (fromMemberId == null) {
            setFromMemberId(new LongFilter());
        }
        return fromMemberId;
    }

    public void setFromMemberId(LongFilter fromMemberId) {
        this.fromMemberId = fromMemberId;
    }

    public LongFilter getToMemberId() {
        return toMemberId;
    }

    public Optional<LongFilter> optionalToMemberId() {
        return Optional.ofNullable(toMemberId);
    }

    public LongFilter toMemberId() {
        if (toMemberId == null) {
            setToMemberId(new LongFilter());
        }
        return toMemberId;
    }

    public void setToMemberId(LongFilter toMemberId) {
        this.toMemberId = toMemberId;
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
        final SettlementCriteria that = (SettlementCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(amount, that.amount) &&
            Objects.equals(eventId, that.eventId) &&
            Objects.equals(fromMemberId, that.fromMemberId) &&
            Objects.equals(toMemberId, that.toMemberId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, amount, eventId, fromMemberId, toMemberId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "SettlementCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalAmount().map(f -> "amount=" + f + ", ").orElse("") +
            optionalEventId().map(f -> "eventId=" + f + ", ").orElse("") +
            optionalFromMemberId().map(f -> "fromMemberId=" + f + ", ").orElse("") +
            optionalToMemberId().map(f -> "toMemberId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
