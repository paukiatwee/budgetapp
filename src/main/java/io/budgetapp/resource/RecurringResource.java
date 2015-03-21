package io.budgetapp.resource;

import io.budgetapp.model.Recurring;
import io.budgetapp.model.User;
import io.budgetapp.model.form.recurring.AddRecurringForm;
import io.budgetapp.service.FinanceService;
import io.dropwizard.auth.Auth;
import io.dropwizard.hibernate.UnitOfWork;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 *
 */
@Path(ResourceURL.RECURRING)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RecurringResource extends AbstractResource {

    private final FinanceService financeService;

    public RecurringResource(FinanceService financeService) {
        this.financeService = financeService;
    }

    @Override
    public String getPath() {
        return ResourceURL.RECURRING;
    }

    @GET
    @UnitOfWork
    public List<Recurring> getRecurrings(@Auth User user) {
        return financeService.findRecurrings(user);
    }

    @POST
    @UnitOfWork
    public Response add(@Auth User user, @Valid AddRecurringForm recurringForm) {
        Recurring recurring = financeService.addRecurring(user, recurringForm);
        return created(recurring, recurring.getId());
    }

    @GET
    @UnitOfWork
    @Path("/{id}/transactions")
    public Response findTransactions(@Auth User user, @PathParam("id") long id) {
        return ok(financeService.findTransactionsByRecurring(user, id));
    }

    @DELETE
    @UnitOfWork
    @Path("/{id}")
    public Response delete(@Auth User user, @PathParam("id") long id) {
        financeService.deleteRecurring(user, id);
        return deleted();
    }

}
