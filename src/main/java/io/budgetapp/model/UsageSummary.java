package io.budgetapp.model;

import java.io.Serializable;

/**
 *
 */
public class UsageSummary implements Serializable {
    private static final long serialVersionUID = 3151593231290523047L;

    private double budget;
    private double spent;

    public UsageSummary(double budget, double spent) {
        this.budget = budget;
        this.spent = spent;
    }

    public double getBudget() {
        return budget;
    }

    public double getSpent() {
        return spent;
    }

    public double getRemaining() {
        return budget - spent;
    }
}
