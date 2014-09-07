package io.budgetapp.model.form;

import io.budgetapp.model.Ledger;
import io.budgetapp.model.RecurringType;

import java.io.Serializable;
import java.util.Date;

/**
 *
 */
public class TransactionForm implements Serializable {

    private static final long serialVersionUID = 1432079737348530213L;

    private double amount;
    private String remark;
    private Date transactionOn;
    private Boolean recurring;
    private RecurringType recurringType;
    private Ledger ledger;

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Date getTransactionOn() {
        if(transactionOn == null) {
            return new Date();
        } else {
            return transactionOn;
        }
    }

    public void setTransactionOn(Date transactionOn) {
        this.transactionOn = transactionOn;
    }

    public Boolean getRecurring() {
        return recurring;
    }

    public void setRecurring(Boolean recurring) {
        this.recurring = recurring;
    }

    public RecurringType getRecurringType() {
        return recurringType;
    }

    public void setRecurringType(RecurringType recurringType) {
        this.recurringType = recurringType;
    }

    public Ledger getLedger() {
        return ledger;
    }

    public void setLedger(Ledger ledger) {
        this.ledger = ledger;
    }

    @Override
    public String toString() {
        return "TransactionForm{" +
                "amount=" + amount +
                ", remark='" + remark + '\'' +
                ", transactionOn=" + transactionOn +
                ", recurring=" + recurring +
                ", recurringType=" + recurringType +
                ", ledger=" + ledger +
                '}';
    }
}
