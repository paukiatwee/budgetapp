package io.budgetapp.model;

import io.budgetapp.model.form.budget.AddBudgetForm;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 *
 */
@Entity
@Table(name = "budgets")
public class Budget implements Serializable {

    private static final long serialVersionUID = 2625666273036891436L;

    private Long id;
    private String name;
    private double projected;
    private double actual;
    private Date period;
    private Date createdAt;
    private User user;
    private Category category;
    private BudgetType budgetType;

    public Budget() {
    }

    public Budget(long budgetId) {
        this.id = budgetId;
    }

    public Budget(AddBudgetForm budgetForm) {
        setName(budgetForm.getName());
        setProjected(budgetForm.getProjected());
        setCategory(new Category(budgetForm.getCategoryId()));
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

    public double getProjected() {
        return projected;
    }

    public void setProjected(double projected) {
        this.projected = projected;
    }

    public double getActual() {
        return actual;
    }

    public void setActual(double actual) {
        this.actual = actual;
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
    public BudgetType getBudgetType() {
        return budgetType;
    }

    public void setBudgetType(BudgetType budgetType) {
        this.budgetType = budgetType;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Budget{");
        sb.append("id=").append(id);
        sb.append(", actual=").append(actual);
        sb.append(", name='").append(name).append('\'');
        sb.append(", createdAt=").append(createdAt);
        sb.append(", user=").append(user);
        sb.append(", category=").append(category);
        sb.append('}');
        return sb.toString();
    }
}
