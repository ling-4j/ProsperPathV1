package com.mycompany.myapp.domain;

import com.mycompany.myapp.domain.enumeration.PeriodType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * A Summary.
 */
@Entity
@Table(name = "summary")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Summary implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false)
    private PeriodType periodType;

    @NotNull
    @Column(name = "period_value", nullable = false)
    private String periodValue;

    @Column(name = "total_assets", precision = 21, scale = 2)
    private BigDecimal totalAssets;

    @Column(name = "total_income", precision = 21, scale = 2)
    private BigDecimal totalIncome;

    @Column(name = "total_expense", precision = 21, scale = 2)
    private BigDecimal totalExpense;

    @Column(name = "total_profit", precision = 21, scale = 2)
    private BigDecimal totalProfit;

    @Column(name = "profit_percentage", precision = 21, scale = 2)
    private BigDecimal profitPercentage;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @ManyToOne(optional = false)
    @NotNull
    private User user;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Summary id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PeriodType getPeriodType() {
        return this.periodType;
    }

    public Summary periodType(PeriodType periodType) {
        this.setPeriodType(periodType);
        return this;
    }

    public void setPeriodType(PeriodType periodType) {
        this.periodType = periodType;
    }

    public String getPeriodValue() {
        return this.periodValue;
    }

    public Summary periodValue(String periodValue) {
        this.setPeriodValue(periodValue);
        return this;
    }

    public void setPeriodValue(String periodValue) {
        this.periodValue = periodValue;
    }

    public BigDecimal getTotalAssets() {
        return this.totalAssets;
    }

    public Summary totalAssets(BigDecimal totalAssets) {
        this.setTotalAssets(totalAssets);
        return this;
    }

    public void setTotalAssets(BigDecimal totalAssets) {
        this.totalAssets = totalAssets;
    }

    public BigDecimal getTotalIncome() {
        return this.totalIncome;
    }

    public Summary totalIncome(BigDecimal totalIncome) {
        this.setTotalIncome(totalIncome);
        return this;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public BigDecimal getTotalExpense() {
        return this.totalExpense;
    }

    public Summary totalExpense(BigDecimal totalExpense) {
        this.setTotalExpense(totalExpense);
        return this;
    }

    public void setTotalExpense(BigDecimal totalExpense) {
        this.totalExpense = totalExpense;
    }

    public BigDecimal getTotalProfit() {
        return this.totalProfit;
    }

    public Summary totalProfit(BigDecimal totalProfit) {
        this.setTotalProfit(totalProfit);
        return this;
    }

    public void setTotalProfit(BigDecimal totalProfit) {
        this.totalProfit = totalProfit;
    }

    public BigDecimal getProfitPercentage() {
        return this.profitPercentage;
    }

    public Summary profitPercentage(BigDecimal profitPercentage) {
        this.setProfitPercentage(profitPercentage);
        return this;
    }

    public void setProfitPercentage(BigDecimal profitPercentage) {
        this.profitPercentage = profitPercentage;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public Summary createdAt(Instant createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return this.updatedAt;
    }

    public Summary updatedAt(Instant updatedAt) {
        this.setUpdatedAt(updatedAt);
        return this;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Summary user(User user) {
        this.setUser(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Summary)) {
            return false;
        }
        return getId() != null && getId().equals(((Summary) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Summary{" +
            "id=" + getId() +
            ", periodType='" + getPeriodType() + "'" +
            ", periodValue='" + getPeriodValue() + "'" +
            ", totalAssets=" + getTotalAssets() +
            ", totalIncome=" + getTotalIncome() +
            ", totalExpense=" + getTotalExpense() +
            ", totalProfit=" + getTotalProfit() +
            ", profitPercentage=" + getProfitPercentage() +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            "}";
    }
}
