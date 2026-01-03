package com.mycompany.myapp.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.mycompany.myapp.domain.EventBalance} entity. This class is used
 * in {@link com.mycompany.myapp.web.rest.EventBalanceResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /event-balances?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class EventBalanceCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private BigDecimalFilter paid;

    private BigDecimalFilter shouldPay;

    private BigDecimalFilter balance;

    private LongFilter eventId;

    private LongFilter memberId;

    private Boolean distinct;

    public EventBalanceCriteria() {}

    public EventBalanceCriteria(EventBalanceCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.paid = other.optionalPaid().map(BigDecimalFilter::copy).orElse(null);
        this.shouldPay = other.optionalShouldPay().map(BigDecimalFilter::copy).orElse(null);
        this.balance = other.optionalBalance().map(BigDecimalFilter::copy).orElse(null);
        this.eventId = other.optionalEventId().map(LongFilter::copy).orElse(null);
        this.memberId = other.optionalMemberId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public EventBalanceCriteria copy() {
        return new EventBalanceCriteria(this);
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

    public BigDecimalFilter getPaid() {
        return paid;
    }

    public Optional<BigDecimalFilter> optionalPaid() {
        return Optional.ofNullable(paid);
    }

    public BigDecimalFilter paid() {
        if (paid == null) {
            setPaid(new BigDecimalFilter());
        }
        return paid;
    }

    public void setPaid(BigDecimalFilter paid) {
        this.paid = paid;
    }

    public BigDecimalFilter getShouldPay() {
        return shouldPay;
    }

    public Optional<BigDecimalFilter> optionalShouldPay() {
        return Optional.ofNullable(shouldPay);
    }

    public BigDecimalFilter shouldPay() {
        if (shouldPay == null) {
            setShouldPay(new BigDecimalFilter());
        }
        return shouldPay;
    }

    public void setShouldPay(BigDecimalFilter shouldPay) {
        this.shouldPay = shouldPay;
    }

    public BigDecimalFilter getBalance() {
        return balance;
    }

    public Optional<BigDecimalFilter> optionalBalance() {
        return Optional.ofNullable(balance);
    }

    public BigDecimalFilter balance() {
        if (balance == null) {
            setBalance(new BigDecimalFilter());
        }
        return balance;
    }

    public void setBalance(BigDecimalFilter balance) {
        this.balance = balance;
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

    public LongFilter getMemberId() {
        return memberId;
    }

    public Optional<LongFilter> optionalMemberId() {
        return Optional.ofNullable(memberId);
    }

    public LongFilter memberId() {
        if (memberId == null) {
            setMemberId(new LongFilter());
        }
        return memberId;
    }

    public void setMemberId(LongFilter memberId) {
        this.memberId = memberId;
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
        final EventBalanceCriteria that = (EventBalanceCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(paid, that.paid) &&
            Objects.equals(shouldPay, that.shouldPay) &&
            Objects.equals(balance, that.balance) &&
            Objects.equals(eventId, that.eventId) &&
            Objects.equals(memberId, that.memberId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, paid, shouldPay, balance, eventId, memberId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "EventBalanceCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalPaid().map(f -> "paid=" + f + ", ").orElse("") +
            optionalShouldPay().map(f -> "shouldPay=" + f + ", ").orElse("") +
            optionalBalance().map(f -> "balance=" + f + ", ").orElse("") +
            optionalEventId().map(f -> "eventId=" + f + ", ").orElse("") +
            optionalMemberId().map(f -> "memberId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
