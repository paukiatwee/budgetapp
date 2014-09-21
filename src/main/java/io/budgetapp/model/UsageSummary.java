package io.budgetapp.model;

import java.io.Serializable;

/**
 *
 */
public class UsageSummary implements Serializable {
    private static final long serialVersionUID = 3151593231290523047L;

    private double projected;
    private double actual;

    public UsageSummary(double projected, double actual) {
        this.projected = projected;
        this.actual = actual;
    }

    public double getProjected() {
        return projected;
    }

    public double getActual() {
        return actual;
    }

    public double getRemaining() {
        return projected - actual;
    }
}
