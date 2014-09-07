package io.budgetapp.application;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.budgetapp.model.ValidationMessage;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.Set;

/**
 *
 */
@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
    private static final int UNPROCESSABLE_ENTITY = 422;

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        ValidationMessage validationMessage = new ValidationMessage();
        Set<ConstraintViolation<?>> violations = exception.getConstraintViolations();
        Multimap<String, String> errors = ArrayListMultimap.create();
        for (ConstraintViolation<?> v : violations) {
            errors.put(v.getPropertyPath().toString(), v.getMessage());
        }

        validationMessage.setErrors(errors.asMap());

        return Response.status(UNPROCESSABLE_ENTITY)
                .entity(validationMessage)
                .build();
    }
}
