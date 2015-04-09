package io.budgetapp.application;

import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(SQLConstraintViolationExceptionMapper.class);

    @Override
    public Response toResponse(ConstraintViolationException e) {
        return Response.status(Response.Status.BAD_REQUEST).entity(Collections.singletonMap("errors", Collections.singletonMap("message", getMessage(e)))).build();
    }

    private List<String> getMessage(ConstraintViolationException e) {

        LOGGER.error(e.getMessage(), e);

        // a hack to convert exception to friendly error message
        if("fk_budgets_categories".equalsIgnoreCase(e.getConstraintName())) {
            return Collections.singletonList("Failed to delete category due to references to existing budget(s).");
        } else if("fk_transactions_budgets".equalsIgnoreCase(e.getConstraintName()))  {
            return Collections.singletonList("Failed to delete budget due to references to existing transaction(s).");
        }

        return Collections.singletonList(e.getMessage());
    }
}
