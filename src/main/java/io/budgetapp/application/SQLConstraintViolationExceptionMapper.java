package io.budgetapp.application;

import org.hibernate.exception.ConstraintViolationException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.Collections;
import java.util.List;

/**
 *
 */
@Provider
public class SQLConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException e) {
        return Response.status(Response.Status.BAD_REQUEST).entity(Collections.singletonMap("errors", Collections.singletonMap("message", getMessage(e)))).build();
    }

    private List<String> getMessage(ConstraintViolationException e) {
        if("fk_budgets_categories".equalsIgnoreCase(e.getConstraintName())) {
            return Collections.singletonList("Failed to delete category due to references to existing budget(s).");
        } else if("fk_transactions_budgets".equalsIgnoreCase(e.getConstraintName()))  {
            return Collections.singletonList("Failed to delete budget due to references to existing transaction(s).");
        }
        return Collections.singletonList(e.getMessage());
    }
}
