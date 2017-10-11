package io.budgetapp.resource;

import io.budgetapp.model.Point;
import io.budgetapp.model.Transaction;
import io.budgetapp.model.User;
import io.budgetapp.model.form.TransactionForm;
import io.budgetapp.service.FinanceService;
import io.dropwizard.auth.Auth;
import io.dropwizard.hibernate.UnitOfWork;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
@Path(ResourceURL.TRANSACTION)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransactionResource extends AbstractResource {

    private final FinanceService financeService;

    public TransactionResource(FinanceService financeService) {
        this.financeService = financeService;
    }

    @Override
    public String getPath() {
        return ResourceURL.TRANSACTION;
    }

    @GET
    @UnitOfWork
    public List<Transaction> findAllTransactions(@Auth User user, @QueryParam("limit") Integer limit) {
        return financeService.findRecentTransactions(user, limit);
    }

    @POST
    @UnitOfWork
    public Response add(@Auth User user, TransactionForm transactionForm) {
        Transaction transaction = financeService.addTransaction(user, transactionForm);
        return created(transaction, transaction.getId());
    }

    @POST
    @UnitOfWork
    @Path("/batched")
    public List<Response> add(@Auth User user, List<TransactionForm> transactionForms) {
        List<Transaction> transactions = financeService.addTransactions(user, transactionForms);
        return transactions
                .stream()
                .map(transaction -> created(transaction, transaction.getId()))
                .collect(Collectors.toList());
    }

    @GET
    @UnitOfWork
    @Path("/{id}")
    public Transaction findById(@PathParam("id") long id) {
        return financeService.findTransactionById(id);
    }

    @DELETE
    @UnitOfWork
    @Path("/{id}")
    public Response delete(@Auth User user, @PathParam("id") long id) {
        boolean deleted = financeService.deleteTransaction(user, id);
        if(deleted) {
            return deleted();
        } else {
            return notFound();
        }
    }

    @GET
    @UnitOfWork
    @Path("/summary")
    public List<Point> findSummary(@Auth User user, @QueryParam("month") Integer month, @QueryParam("year") Integer year) {
        return financeService.findTransactionUsage(user, month, year);
    }

    @GET
    @UnitOfWork
    @Path("/monthly")
    public List<Point> findMonthly(@Auth User user) {
        return financeService.findMonthlyTransactionUsage(user);
    }

    @GET
    @UnitOfWork
    @Path("/today")
    public Response findTodayRecurringTransactions(@Auth User user) {
        return ok(financeService.findTodayRecurringsTransactions(user));
    }

}
