package io.budgetapp.resource;

import io.budgetapp.model.AccountSummary;
import io.budgetapp.model.UsageSummary;
import io.budgetapp.model.User;
import io.budgetapp.model.form.LoginForm;
import io.budgetapp.model.form.SignUpForm;
import io.budgetapp.model.form.user.Password;
import io.budgetapp.model.form.user.Profile;
import io.budgetapp.service.FinanceService;
import io.dropwizard.auth.Auth;
import io.dropwizard.hibernate.UnitOfWork;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

/**
 *
 */
@Path(ResourceURL.USER)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource extends AbstractResource {

    private final FinanceService financeService;

    public UserResource(FinanceService financeService) {
        this.financeService = financeService;
    }

    @Override
    public String getPath() {
        return ResourceURL.USER;
    }

    @POST
    @UnitOfWork
    public Response add(@Valid SignUpForm signUp) {
        User user = financeService.addUser(signUp);
        return created(user, user.getId());
    }

    @PUT
    @UnitOfWork
    public Response update(@Auth User user, Profile profile) {
        return ok(financeService.update(user, profile));
    }

    @PUT
    @UnitOfWork
    @Path("/password")
    public Response changePassword(@Auth User user, @Valid Password password) {
        financeService.changePassword(user, password);
        return ok();
    }

    @GET
    @UnitOfWork
    @Path("/account")
    public AccountSummary findAccountSummary(@Auth User user, @QueryParam("month") Integer month, @QueryParam("year") Integer year) {
        return financeService.findAccountSummaryByUser(user, month, year);
    }

    @GET
    @UnitOfWork
    @Path("/usage")
    public UsageSummary findUsageSummary(@Auth User user, @QueryParam("month") Integer month, @QueryParam("year") Integer year) {
        return financeService.findUsageSummaryByUser(user, month, year);
    }


    @POST
    @UnitOfWork
    @Path("/auth")
    public Response login(LoginForm login) {
        Optional<User> user = financeService.login(login);
        return user.map(this::ok).orElseGet(this::unauthorized);
    }

    /**
     * return current user
     */
    @GET
    @UnitOfWork
    @Path("/ping")
    public Response ping(@Auth User user) {
        if(user != null) {
            return ok(user);
        } else {
            return unauthorized();
        }
    }
}
