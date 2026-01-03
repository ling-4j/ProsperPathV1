package com.mycompany.myapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * A Settlement.
 */
@Entity
@Table(name = "settlement")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Settlement implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "amount", precision = 21, scale = 2, nullable = false)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnoreProperties(value = { "team", "keyPayer" }, allowSetters = true)
    private Event event;

    @ManyToOne(fetch = FetchType.EAGER)
    private Member fromMember;

    @ManyToOne(fetch = FetchType.EAGER)
    private Member toMember;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Settlement id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public Settlement amount(BigDecimal amount) {
        this.setAmount(amount);
        return this;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Event getEvent() {
        return this.event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Settlement event(Event event) {
        this.setEvent(event);
        return this;
    }

    public Member getFromMember() {
        return this.fromMember;
    }

    public void setFromMember(Member member) {
        this.fromMember = member;
    }

    public Settlement fromMember(Member member) {
        this.setFromMember(member);
        return this;
    }

    public Member getToMember() {
        return this.toMember;
    }

    public void setToMember(Member member) {
        this.toMember = member;
    }

    public Settlement toMember(Member member) {
        this.setToMember(member);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Settlement)) {
            return false;
        }
        return getId() != null && getId().equals(((Settlement) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Settlement{" +
            "id=" + getId() +
            ", amount=" + getAmount() +
            "}";
    }
}
