package io.budgetapp.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 *
 */
@Entity
@Table(name = "budget_types")
public class BudgetType implements Serializable {

    private static final long serialVersionUID = -7580231307267509312L;

    private Long id;
    private Date createdAt;
    private List<Budget> budgets;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
    @OneToMany()
    public List<Budget> getBudgets() {
        return budgets;
    }

    public void setBudgets(List<Budget> budgets) {
        this.budgets = budgets;
    }

    @Override
    public String toString() {
        return "BudgetType{" +
                "id=" + id +
                ", createdAt=" + createdAt +
                ", budgets=" + budgets +
                '}';
    }
}
