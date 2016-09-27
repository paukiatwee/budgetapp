package io.budgetapp.application;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.budgetapp.model.ValidationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.Set;
import java.util.stream.StreamSupport;

/**
 *
 */
@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConstraintViolationExceptionMapper.class);

    @Override
    public Response toResponse(ConstraintViolationException exception) {

        LOGGER.debug("Validation constraint violation {}", exception.getConstraintViolations());

        ValidationMessage validationMessage = new ValidationMessage();
        Set<ConstraintViolation<?>> violations = exception.getConstraintViolations();
        Multimap<String, String> errors = ArrayListMultimap.create();
        for (ConstraintViolation<?> cv : violations) {
            String name = StreamSupport.stream(cv.getPropertyPath().spliterator(), false)
                    .map(Path.Node::getName)
                    .reduce((first, second) -> second)
                    .orElseGet(() -> cv.getPropertyPath().toString());
            errors.put(name, cv.getMessage());
        }

        validationMessage.setErrors(errors.asMap());

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(validationMessage)
                .build();
    }
}
