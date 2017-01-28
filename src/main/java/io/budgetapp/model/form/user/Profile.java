package io.budgetapp.model.form.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Profile {

    private String name;
    private String currency;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
