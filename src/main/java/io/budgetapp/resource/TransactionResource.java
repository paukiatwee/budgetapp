package io.budgetapp.resource;

import io.budgetapp.model.Point;
import io.budgetapp.model.Transaction;
import io.budgetapp.model.User;
import io.budgetapp.model.form.TransactionForm;
import io.budgetapp.service.FinanceService;
import io.dropwizard.auth.Auth;
import io.dropwizard.hibernate.UnitOfWork;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 *
 */
@Path("/api/transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransactionResource extends AbstractResource {

    private final FinanceService financeService;

    public TransactionResource(FinanceService financeService) {
        this.financeService = financeService;
    }

    @GET
    @UnitOfWork
    public List<Transaction> findAllTransactions(@Auth User user) {
        return financeService.findTransactions(user);
    }

    @POST
    @UnitOfWork
    public Response add(@Auth User user, TransactionForm transactionForm) {
        Transaction transaction = financeService.addTransaction(user, transactionForm);
        return created(transaction, transaction.getId());
    }

    @GET
    @UnitOfWork
    @Path("/{id}")
    public Transaction findById(@PathParam("id") long id) {
        return financeService.findTransactionById(id);
    }

    @GET
    @UnitOfWork
    @Path("/summary")
    public List<Point> findSummary(@Auth User user) {
        return financeService.findTransactionUsage(user);
    }

    @GET
    @UnitOfWork
    @Path("/monthly")
    public List<Point> findMonthly(@Auth User user) {
        return financeService.findMonthlyTransactionUsage(user);
    }

    @GET
    @UnitOfWork
    @Path("/recent")
    public List<Transaction> findRecentTransactions(@Auth User user, @QueryParam("limit") Integer limit) {
        return financeService.findRecentTransactions(user, limit);
    }
}
