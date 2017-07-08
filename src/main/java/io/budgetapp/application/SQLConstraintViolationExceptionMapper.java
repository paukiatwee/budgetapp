package io.budgetapp.application;

import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.Collections;
import java.util.List;

/**
 *
 */
@Provider
public class SQLConstraintViolationExceptionMapper implements ExceptionMapper<PersistenceException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SQLConstraintViolationExceptionMapper.class);

    @Override
    public Response toResponse(PersistenceException e) {
        return Response.status(Response.Status.BAD_REQUEST).entity(Collections.singletonMap("errors", Collections.singletonMap("message", getMessage(e)))).build();
    }

    private List<String> getMessage(PersistenceException e) {

        LOGGER.error(e.getMessage(), e);

        if(e.getCause() instanceof ConstraintViolationException) {
            // a hack to convert exception to friendly error message
            ConstraintViolationException cve = (ConstraintViolationException) e.getCause();
            if ("fk_budgets_categories".equalsIgnoreCase(cve.getConstraintName())) {
                return Collections.singletonList("Failed to delete category due to references to existing budget(s).");
            } else if ("fk_transactions_budgets".equalsIgnoreCase(cve.getConstraintName())) {
                return Collections.singletonList("Failed to delete budget due to references to existing transaction(s).");
            }
            return Collections.singletonList(e.getMessage());
        } else {
            return Collections.singletonList(e.getMessage());
        }
    }
}
