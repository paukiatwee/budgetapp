package io.budgetapp.model.form.report;

import java.io.Serializable;
import java.util.Date;

/**
 *
 */
public class SearchFilter implements Serializable {

    private static final long serialVersionUID = 2152292419706145722L;

    private Double maxAmount;
    private Double minAmount;
    private Date startOn;
    private Date endOn;

    public Double getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(Double maxAmount) {
        this.maxAmount = maxAmount;
    }

    public Double getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(Double minAmount) {
        this.minAmount = minAmount;
    }

    public Date getStartOn() {
        return startOn;
    }

    public void setStartOn(Date startOn) {
        this.startOn = startOn;
    }

    public Date getEndOn() {
        return endOn;
    }

    public void setEndOn(Date endOn) {
        this.endOn = endOn;
    }

    public boolean isDateRange() {
        return getStartOn() != null && getEndOn() != null;
    }

    public boolean isAmountRange() {
        return getMaxAmount() != null && getMinAmount() != null;
    }

    @Override
    public String toString() {
        return "SearchFilter{" +
                "maxAmount=" + maxAmount +
                ", minAmount=" + minAmount +
                ", startOn=" + startOn +
                ", endOn=" + endOn +
                '}';
    }
}
