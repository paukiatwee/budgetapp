package io.budgetapp.resource;

import com.google.common.io.Resources;
import io.budgetapp.client.HTTPTokenClientFilter;
import io.budgetapp.modal.IdentityResponse;
import io.budgetapp.model.Budget;
import io.budgetapp.model.Category;
import io.budgetapp.model.CategoryType;
import io.budgetapp.model.User;
import io.budgetapp.model.form.SignUpForm;
import io.budgetapp.model.form.budget.AddBudgetForm;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.logging.LoggingFeature;
import org.junit.BeforeClass;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 */
public abstract class ResourceIT {

    private static final Logger LOGGER = Logger.getLogger(ResourceIT.class.getName());

    protected static Client client;
    protected static User defaultUser;
    protected static Category defaultCategory;
    protected static Budget defaultBudget;

    @BeforeClass
    public static void before() {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.register(new LoggingFeature(LOGGER, LoggingFeature.Verbosity.PAYLOAD_ANY));
        client = ClientBuilder.newClient(clientConfig);

        SignUpForm signUp = new SignUpForm();

        signUp.setUsername(randomEmail());
        signUp.setPassword(randomAlphabets());
        post(ResourceURL.USER, signUp);
        Response authResponse = post("/api/users/auth", signUp);
        defaultUser = authResponse.readEntity(User.class);
        defaultUser.setUsername(signUp.getUsername());
        defaultUser.setPassword(signUp.getPassword());

        client.register(new HTTPTokenClientFilter(defaultUser.getToken()));

        defaultCategory = new Category();
        defaultCategory.setName(randomAlphabets());
        defaultCategory.setType(CategoryType.EXPENDITURE);

        Response response = post(ResourceURL.CATEGORY, defaultCategory);
        String location = response.getLocation().toString();
        String[] raw = location.split("/");
        defaultCategory.setId(Long.valueOf(raw[raw.length - 1]));

        defaultBudget = new Budget();
        defaultBudget.setName(randomAlphabets());
        defaultBudget.setCategory(defaultCategory);
        AddBudgetForm addBudgetForm = new AddBudgetForm();
        addBudgetForm.setName(defaultBudget.getName());
        addBudgetForm.setCategoryId(defaultCategory.getId());
        response = post(ResourceURL.BUDGET, addBudgetForm);
        location = response.getLocation().toString();
        raw = location.split("/");
        defaultBudget.setId(Long.valueOf(raw[raw.length - 1]));

    }

    protected static Response post(String path, Object entity) {
        return client
                .target(getUrl(path))
                .request(MediaType.APPLICATION_JSON_TYPE)
                .buildPost(Entity.json(entity)).invoke();
    }

    protected static Response put(String path, Object entity) {
        return client
                .target(getUrl(path))
                .request(MediaType.APPLICATION_JSON_TYPE)
                .buildPut(Entity.json(entity)).invoke();
    }

    protected static Response delete(String path) {
        return client
                .target(getUrl(path))
                .request(MediaType.APPLICATION_JSON_TYPE)
                .buildDelete().invoke();
    }

    protected Response get(String path) {
        return client
                .target(getUrl(path))
                .request(MediaType.APPLICATION_JSON_TYPE)
                .buildGet().invoke();
    }

    protected IdentityResponse identityResponse(Response response) {
        return response.readEntity(IdentityResponse.class);
    }

    protected List<IdentityResponse> identityResponses(Response response) {
        return response.readEntity(new GenericType<List<IdentityResponse>>() {});
    }

    protected void assertCreated(Response response) {
        assertThat(response.getStatus(), is(201));
    }

    protected void assertOk(Response response) {
        assertThat(response.getStatus(), is(200));
    }

    protected void assertDeleted(Response response) {
        assertThat(response.getStatus(), is(Response.Status.NO_CONTENT.getStatusCode()));
    }

    protected void assertNotFound(Response response) {
        assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
    }

    protected void assertBadRequest(Response response) {
        assertThat(response.getStatus(), is(400));
    }

    protected static String randomAlphabets() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    protected static String randomEmail() {
        return randomAlphabets() + "@" + randomAlphabets() + ".com";
    }

    protected static String getUrl() {
        return "http://localhost:9999";
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
