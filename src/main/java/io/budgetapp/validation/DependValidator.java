package io.budgetapp.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 *
 */
public class DependValidator implements ConstraintValidator<Depend, Object> {

    private String fieldName;

    @Override
    public void initialize(Depend annotation) {
        fieldName = annotation.fieldName();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        return false;
    }
}
