package io.budgetapp.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 *
 */
@Entity
@Table(name = "transactions")
public class Transaction implements Serializable {

    private static final long serialVersionUID = 2625666273036891436L;

    private Long id;
    private String name;
    private double amount;
    private String remark;
    private boolean auto;
    private Date transactionOn;
    private Date createdAt;
    private Budget budget;
    private Recurring recurring;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(nullable = false, updatable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(nullable = false, updatable = false)
    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Column(updatable = false)
    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public boolean isAuto() {
        return auto;
    }

    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "transaction_on", nullable = false, updatable = false)
    public Date getTransactionOn() {
        return transactionOn;
    }

    public void setTransactionOn(Date transactionOn) {
        this.transactionOn = transactionOn;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", insertable = false, nullable = false, updatable = false)
    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @JoinColumn(updatable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    public Budget getBudget() {
        return budget;
    }

    public void setBudget(Budget budget) {
        this.budget = budget;
    }

    @JoinColumn(updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    public Recurring getRecurring() {
        return recurring;
    }

    public void setRecurring(Recurring recurring) {
        this.recurring = recurring;
    }

    @PrePersist
    void perPersist() {
        if (transactionOn == null) {
            transactionOn = new Date();
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Transaction{");
        sb.append("id=").append(id);
        sb.append(", amount=").append(amount);
        sb.append(", remark='").append(remark).append('\'');
        sb.append(", transactionOn=").append(transactionOn);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", budget=").append(budget);
        sb.append('}');
        return sb.toString();
    }
}
