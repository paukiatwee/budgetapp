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
        if("fk_ledgers_categories".equalsIgnoreCase(e.getConstraintName())) {
            return Collections.singletonList("Failed to delete category due to references to existing ledger(s).");
        } else if("fk_transactions_ledgers".equalsIgnoreCase(e.getConstraintName()))  {
            return Collections.singletonList("Failed to delete ledger due to references to existing transaction(s).");
        }
        return Collections.singletonList(e.getMessage());
    }
}
