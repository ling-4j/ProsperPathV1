package com.mycompany.myapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * A BillParticipant.
 */
@Entity
@Table(name = "bill_participant")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class BillParticipant implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "share_amount", precision = 21, scale = 2, nullable = false)
    private BigDecimal shareAmount;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnoreProperties(value = { "event", "payer" }, allowSetters = true)
    private Bill bill;

    @ManyToOne(fetch = FetchType.EAGER)
    private Member member;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public BillParticipant id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getShareAmount() {
        return this.shareAmount;
    }

    public BillParticipant shareAmount(BigDecimal shareAmount) {
        this.setShareAmount(shareAmount);
        return this;
    }

    public void setShareAmount(BigDecimal shareAmount) {
        this.shareAmount = shareAmount;
    }

    public Bill getBill() {
        return this.bill;
    }

    public void setBill(Bill bill) {
        this.bill = bill;
    }

    public BillParticipant bill(Bill bill) {
        this.setBill(bill);
        return this;
    }

    public Member getMember() {
        return this.member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public BillParticipant member(Member member) {
        this.setMember(member);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BillParticipant)) {
            return false;
        }
        return getId() != null && getId().equals(((BillParticipant) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "BillParticipant{" +
            "id=" + getId() +
            ", shareAmount=" + getShareAmount() +
            "}";
    }
}
