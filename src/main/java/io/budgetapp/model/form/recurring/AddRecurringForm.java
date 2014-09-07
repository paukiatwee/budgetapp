package io.budgetapp.model.form.recurring;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.budgetapp.model.RecurringType;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddRecurringForm implements Serializable {

    private static final long serialVersionUID = -3317443535487916735L;

    private Long ledgerId;
    private Double amount;
    private RecurringType recurringType;

    @NotNull(message = "{validation.ledger.required}")
    public Long getLedgerId() {
        return ledgerId;
    }

    public void setLedgerId(Long ledgerId) {
        this.ledgerId = ledgerId;
    }

    @NotNull(message = "{validation.amount.required}")
    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    @NotNull(message = "{validation.recurringType.required}")
    public RecurringType getRecurringType() {
        return recurringType;
    }

    public void setRecurringType(RecurringType recurringType) {
        this.recurringType = recurringType;
    }
}
