package com.mycompany.myapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * A EventBalance.
 */
@Entity
@Table(name = "event_balance")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class EventBalance implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "paid", precision = 21, scale = 2, nullable = false)
    private BigDecimal paid;

    @NotNull
    @Column(name = "should_pay", precision = 21, scale = 2, nullable = false)
    private BigDecimal shouldPay;

    @NotNull
    @Column(name = "balance", precision = 21, scale = 2, nullable = false)
    private BigDecimal balance;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnoreProperties(value = { "team", "keyPayer" }, allowSetters = true)
    private Event event;

    @ManyToOne(fetch = FetchType.EAGER)
    private Member member;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public EventBalance id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getPaid() {
        return this.paid;
    }

    public EventBalance paid(BigDecimal paid) {
        this.setPaid(paid);
        return this;
    }

    public void setPaid(BigDecimal paid) {
        this.paid = paid;
    }

    public BigDecimal getShouldPay() {
        return this.shouldPay;
    }

    public EventBalance shouldPay(BigDecimal shouldPay) {
        this.setShouldPay(shouldPay);
        return this;
    }

    public void setShouldPay(BigDecimal shouldPay) {
        this.shouldPay = shouldPay;
    }

    public BigDecimal getBalance() {
        return this.balance;
    }

    public EventBalance balance(BigDecimal balance) {
        this.setBalance(balance);
        return this;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Event getEvent() {
        return this.event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public EventBalance event(Event event) {
        this.setEvent(event);
        return this;
    }

    public Member getMember() {
        return this.member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public EventBalance member(Member member) {
        this.setMember(member);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EventBalance)) {
            return false;
        }
        return getId() != null && getId().equals(((EventBalance) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "EventBalance{" +
            "id=" + getId() +
            ", paid=" + getPaid() +
            ", shouldPay=" + getShouldPay() +
            ", balance=" + getBalance() +
            "}";
    }
}
