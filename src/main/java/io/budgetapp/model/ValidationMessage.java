package io.budgetapp.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 *
 */
public class ValidationMessage implements Serializable {

    private static final long serialVersionUID = 4630394887642028993L;

    private Map<String, Collection<String>> errors;

    public ValidationMessage() {
    }

    public ValidationMessage(String error, Collection<String> messages) {
        this.errors = Collections.singletonMap(error, messages);
    }

    public Map<String, Collection<String>> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, Collection<String>> errors) {
        this.errors = errors;
    }
}
