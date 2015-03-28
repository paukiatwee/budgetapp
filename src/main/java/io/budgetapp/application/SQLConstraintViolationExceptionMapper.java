package io.budgetapp.application;

import org.glassfish.jersey.server.spi.ResponseErrorMapper;
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
public class SQLConstraintViolationExceptionMapper implements ResponseErrorMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(SQLConstraintViolationExceptionMapper.class);

    @Override
    public Response toResponse(Throwable e) {
        return Response.status(Response.Status.BAD_REQUEST).entity(Collections.singletonMap("errors", Collections.singletonMap("message", getMessage(e)))).build();
    }

    private List<String> getMessage(Throwable e) {

        LOGGER.error(e.getMessage(), e);

        if(e instanceof ConstraintViolationException) {
            ConstraintViolationException constraintViolationException = (ConstraintViolationException) e;
            // a hack to convert exception to friendly error message
            if("fk_budgets_categories".equalsIgnoreCase(constraintViolationException.getConstraintName())) {
                return Collections.singletonList("Failed to delete category due to references to existing budget(s).");
            } else if("fk_transactions_budgets".equalsIgnoreCase(constraintViolationException.getConstraintName()))  {
                return Collections.singletonList("Failed to delete budget due to references to existing transaction(s).");
            }
        }

        return Collections.singletonList(e.getMessage());
    }
}
