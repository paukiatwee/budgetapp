package io.budgetapp.resource;

import com.google.common.io.Resources;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.filter.LoggingFilter;
import io.budgetapp.client.HTTPTokenClientFilter;
import io.budgetapp.modal.IdentityResponse;
import io.budgetapp.model.Budget;
import io.budgetapp.model.Category;
import io.budgetapp.model.CategoryType;
import io.budgetapp.model.User;
import io.budgetapp.model.form.SignUpForm;
import io.budgetapp.model.form.budget.AddBudgetForm;
import org.junit.BeforeClass;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 */
public abstract class ResourceIT {

    protected static Client client;
    protected static User user;
    protected static Category defaultCategory;
    protected static Budget defaultBudget;

    @BeforeClass
    public static void before() {
        client = new Client();
        client.addFilter(new LoggingFilter(System.out));

        SignUpForm signUp = new SignUpForm();

        signUp.setUsername(randomEmail());
        signUp.setPassword(randomAlphabets());
        post("/api/users", signUp);
        ClientResponse authResponse = post("/api/users/auth", signUp);
        User user = authResponse.getEntity(User.class);

        client.addFilter(new HTTPTokenClientFilter(user.getToken()));

        defaultCategory = new Category();
        defaultCategory.setName(randomAlphabets());
        defaultCategory.setType(CategoryType.EXPENDITURE);

        ClientResponse response = post("/api/categories", defaultCategory);
        String location = response.getLocation().toString();
        String[] raw = location.split("/");
        defaultCategory.setId(Long.valueOf(raw[raw.length - 1]));

        defaultBudget = new Budget();
        defaultBudget.setName(randomAlphabets());
        defaultBudget.setCategory(defaultCategory);
        AddBudgetForm addBudgetForm = new AddBudgetForm();
        addBudgetForm.setName(defaultBudget.getName());
        addBudgetForm.setCategoryId(defaultCategory.getId());
        response = post("/api/budgets", addBudgetForm);
        location = response.getLocation().toString();
        raw = location.split("/");
        defaultBudget.setId(Long.valueOf(raw[raw.length - 1]));

    }

    protected static ClientResponse post(String path, Object entity) {
        return client
                .resource(getUrl(path))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class, entity);
    }

    protected static ClientResponse put(String path, Object entity) {
        return client
                .resource(getUrl(path))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .put(ClientResponse.class, entity);
    }

    protected static ClientResponse delete(String path) {
        return client
                .resource(getUrl(path))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .delete(ClientResponse.class);
    }

    protected ClientResponse get(String path) {
        return client
                .resource(getUrl(path))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .get(ClientResponse.class);
    }

    protected IdentityResponse identityResponse(ClientResponse response) {
        return response.getEntity(IdentityResponse.class);
    }

    protected List<IdentityResponse> identityResponses(ClientResponse response) {
        return response.getEntity(new GenericType<List<IdentityResponse>>(){});
    }

    protected void assertCreated(ClientResponse response) {
        assertThat(response.getStatus(), is(201));
    }

    protected void assertOk(ClientResponse response) {
        assertThat(response.getStatus(), is(200));
    }

    protected void assertBadRequest(ClientResponse response) {
        assertThat(response.getStatus(), is(400));
    }

    protected static String randomAlphabets() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    protected static String randomEmail() {
        return randomAlphabets() + "@" + randomAlphabets() + ".com";
    }

    protected static String getUrl() {
        return String.format("http://localhost:8080");
    }

    protected abstract int getPort();

    protected static String getUrl(String path) {
        return getUrl() + path;
    }

    protected static String resourceFilePath(String resourceClassPathLocation) {
        try {
            return new File(Resources.getResource(resourceClassPathLocation).toURI()).getAbsolutePath();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
