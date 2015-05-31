package io.budgetapp.resource;

import io.budgetapp.model.Budget;
import io.budgetapp.model.Category;
import io.budgetapp.model.Point;
import io.budgetapp.model.User;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 *
 */
@Path(ResourceURL.CATEGORY)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CategoryResource extends AbstractResource {

    private final FinanceService financeService;

    public CategoryResource(FinanceService financeService) {
        this.financeService = financeService;
    }

    @Override
    public String getPath() {
        return ResourceURL.CATEGORY;
    }

    @GET
    @UnitOfWork
    public List<Category> findAll(@Auth User user) {
        return financeService.findCategories(user);
    }

    @POST
    @UnitOfWork
    public Response add(@Auth User user, @Valid Category category) {
        category = financeService.addCategory(user, category);
        return created(category, category.getId());
    }

    @DELETE
    @UnitOfWork
    @Path("/{id}")
    public Response delete(@Auth User user, @PathParam("id") long id) {
        financeService.deleteCategory(user, id);
        return deleted();
    }

    @GET
    @UnitOfWork
    @Path("/{id}")
    public Category findById(@PathParam("id") long id) {
        return financeService.findCategoryById(id);
    }


    @GET
    @UnitOfWork
    @Path("/{id}/budgets")
    public List<Budget> findBudgets(@Auth User user, @PathParam("id") long id) {
        return financeService.findBudgetsByCategory(user, id);
    }

    @GET
    @UnitOfWork
    @Path("/summary")
    public List<Point> findSummary(@Auth User user, @QueryParam("month") Integer month, @QueryParam("year") Integer year) {
        return financeService.findUsageByCategory(user, month, year);
    }

    @GET
    @UnitOfWork
    @Path("/suggests")
    public List<String> findSuggestion(@Auth User user, @QueryParam("q") String q) {
        return financeService.findCategorySuggestions(user, q);
    }

}
