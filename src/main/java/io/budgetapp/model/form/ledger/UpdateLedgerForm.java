package io.budgetapp.model.form.ledger;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateLedgerForm extends LedgerForm implements Serializable {

    private static final long serialVersionUID = 7677505567308081026L;

    private Long id;

    @NotNull(message = "{validation.id.required}")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
