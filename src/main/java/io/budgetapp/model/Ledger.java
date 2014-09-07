package io.budgetapp.model;

import io.budgetapp.model.form.ledger.AddLedgerForm;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.budgetapp.model.form.ledger.AddLedgerForm;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 *
 */
@Entity
@Table(name = "ledgers")
public class Ledger implements Serializable {

    private static final long serialVersionUID = 2625666273036891436L;

    private Long id;
    private String name;
    private double budget;
    private double spent;
    private Date period;
    private Date createdAt;
    private User user;
    private Category category;
    private LedgerType ledgerType;

    public Ledger() {
    }

    public Ledger(long ledgerId) {
        this.id = ledgerId;
    }

    public Ledger(AddLedgerForm ledgerForm) {
        setName(ledgerForm.getName());
        setBudget(ledgerForm.getBudget());
        setCategory(new Category(ledgerForm.getCategoryId()));
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public double getSpent() {
        return spent;
    }

    public void setSpent(double spent) {
        this.spent = spent;
    }


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "period_on", nullable = false, updatable = false)
    public Date getPeriod() {
        return period;
    }

    public void setPeriod(Date period) {
        this.period = period;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", insertable = false, nullable = false, updatable = false)
    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @JsonIgnore
    @JoinColumn(updatable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @JoinColumn(updatable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    @JoinColumn(name = "type_id", updatable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    public LedgerType getLedgerType() {
        return ledgerType;
    }

    public void setLedgerType(LedgerType ledgerType) {
        this.ledgerType = ledgerType;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Ledger{");
        sb.append("id=").append(id);
        sb.append(", spent=").append(spent);
        sb.append(", name='").append(name).append('\'');
        sb.append(", createdAt=").append(createdAt);
        sb.append(", user=").append(user);
        sb.append(", category=").append(category);
        sb.append('}');
        return sb.toString();
    }
}
