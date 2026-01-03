package com.mycompany.myapp.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.mycompany.myapp.domain.BillParticipant} entity. This class is used
 * in {@link com.mycompany.myapp.web.rest.BillParticipantResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /bill-participants?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class BillParticipantCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private BigDecimalFilter shareAmount;

    private LongFilter billId;

    private LongFilter memberId;

    private Boolean distinct;

    public BillParticipantCriteria() {}

    public BillParticipantCriteria(BillParticipantCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.shareAmount = other.optionalShareAmount().map(BigDecimalFilter::copy).orElse(null);
        this.billId = other.optionalBillId().map(LongFilter::copy).orElse(null);
        this.memberId = other.optionalMemberId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public BillParticipantCriteria copy() {
        return new BillParticipantCriteria(this);
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

    public BigDecimalFilter getShareAmount() {
        return shareAmount;
    }

    public Optional<BigDecimalFilter> optionalShareAmount() {
        return Optional.ofNullable(shareAmount);
    }

    public BigDecimalFilter shareAmount() {
        if (shareAmount == null) {
            setShareAmount(new BigDecimalFilter());
        }
        return shareAmount;
    }

    public void setShareAmount(BigDecimalFilter shareAmount) {
        this.shareAmount = shareAmount;
    }

    public LongFilter getBillId() {
        return billId;
    }

    public Optional<LongFilter> optionalBillId() {
        return Optional.ofNullable(billId);
    }

    public LongFilter billId() {
        if (billId == null) {
            setBillId(new LongFilter());
        }
        return billId;
    }

    public void setBillId(LongFilter billId) {
        this.billId = billId;
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
        final BillParticipantCriteria that = (BillParticipantCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(shareAmount, that.shareAmount) &&
            Objects.equals(billId, that.billId) &&
            Objects.equals(memberId, that.memberId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, shareAmount, billId, memberId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "BillParticipantCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalShareAmount().map(f -> "shareAmount=" + f + ", ").orElse("") +
            optionalBillId().map(f -> "billId=" + f + ", ").orElse("") +
            optionalMemberId().map(f -> "memberId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
