package io.budgetapp.model.form.budget;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Min;
import java.io.Serializable;

/**
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BudgetForm implements Serializable {

    private static final long serialVersionUID = 7677505567308081026L;

    private String name;
    private double projected;

    @NotBlank(message = "{validation.name.required}")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Min(value = 0, message = "{validation.projected.min}")
    public double getProjected() {
        return projected;
    }

    public void setProjected(double projected) {
        this.projected = projected;
    }

}
