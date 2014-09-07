package io.budgetapp.resource;


import com.sun.jersey.api.client.ClientResponse;
import io.budgetapp.BudgetApplication;
import io.budgetapp.configuration.AppConfiguration;
import io.budgetapp.model.form.SignUpForm;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

/**
 *
 */
public class UserResourceIT extends ResourceIT {

    @ClassRule
    public static final DropwizardAppRule<AppConfiguration> RULE =
            new DropwizardAppRule<>(BudgetApplication.class, resourceFilePath("config-test.yml"));

    @Override
    protected int getPort() {
        return RULE.getLocalPort();
    }

    @Test
    public void shouldAbleCreateUser() {

        // give
        SignUpForm signUp = new SignUpForm();

        // when
        signUp.setUsername(randomEmail());
        signUp.setPassword(randomAlphabets());
        ClientResponse response = post("/api/users", signUp);

        // then
        assertCreated(response);
        Assert.assertNotNull(response.getLocation());
    }
}
