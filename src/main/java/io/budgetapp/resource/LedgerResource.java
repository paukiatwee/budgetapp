package io.budgetapp.resource;

import io.budgetapp.model.Ledger;
import io.budgetapp.model.Transaction;
import io.budgetapp.model.User;
import io.budgetapp.model.form.ledger.AddLedgerForm;
import io.budgetapp.model.form.ledger.UpdateLedgerForm;
import io.budgetapp.service.FinanceService;
import io.dropwizard.auth.Auth;
import io.dropwizard.hibernate.UnitOfWork;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
@Path("/api/ledgers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LedgerResource extends AbstractResource {

    private final FinanceService financeService;

    public LedgerResource(FinanceService financeService) {
        this.financeService = financeService;
    }

    @GET
    @UnitOfWork
    public List<Ledger> getLedgers(@Auth User user) {
        return financeService.findLedgersByUser(user);
    }

    @POST
    @UnitOfWork
    public Response add(@Auth User user, @Valid AddLedgerForm ledgerForm) {
        Ledger ledger = financeService.addLedger(user, ledgerForm);
        return created(ledger, ledger.getId());
    }

    @PUT
    @UnitOfWork
    @Path("/{id}")
    public Response update(@Auth User user, @PathParam("id") long id, @Valid UpdateLedgerForm ledgerForm) {
        ledgerForm.setId(id);
        Ledger ledger = financeService.updateLedger(user, ledgerForm);
        return ok(ledger);
    }

    @DELETE
    @UnitOfWork
    @Path("/{id}")
    public Response delete(@Auth User user, @PathParam("id") long id) {
        financeService.deleteLedger(user, id);
        return deleted();
    }

    @GET
    @UnitOfWork
    @Path("/{id}")
    public Ledger findById(@Auth User user, @PathParam("id") long id) {
        return financeService.findLedgerById(user, id);
    }

    @GET
    @UnitOfWork
    @Path("/{id}/transactions")
    public List<Transaction> findTransactions(@Auth User user, @PathParam("id") long id) {
        return financeService.findTransactionsByLedger(user, id);
    }

    @GET
    @UnitOfWork
    @Path("/suggests")
    public List<String> findSuggestion(@Auth User user, @QueryParam("q") String q) {
        return financeService.findLedgerSuggestions(user, q);
    }
}
