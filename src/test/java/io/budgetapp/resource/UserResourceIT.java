package io.budgetapp.resource;


import io.budgetapp.BudgetApplication;
import io.budgetapp.configuration.AppConfiguration;
import io.budgetapp.modal.ErrorResponse;
import io.budgetapp.model.User;
import io.budgetapp.model.form.LoginForm;
import io.budgetapp.model.form.SignUpForm;
import io.budgetapp.model.form.user.Password;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.Arrays;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

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

        // given
        SignUpForm signUp = new SignUpForm();

        // when
        signUp.setUsername(randomEmail());
        signUp.setPassword(randomAlphabets());
        Response response = post(ResourceURL.USER, signUp);

        // then
        assertCreated(response);
        Assert.assertNotNull(response.getLocation());
    }

    @Test
    public void shouldBeAbleValidateSignUpForm() {
        // given
        SignUpForm signUp = new SignUpForm();

        // when
        Response response = post(ResourceURL.USER, signUp);

        // then
        assertBadRequest(response);
        ErrorResponse errorResponse = response.readEntity(ErrorResponse.class);
        assertTrue(errorResponse.getErrors().keySet().containsAll(Arrays.asList("username", "password")));
    }


    @Test
    public void shouldAbleChangePassword() {
        // given
        Password password = new Password();
        password.setOriginal(defaultUser.getPassword());
        password.setPassword(randomAlphabets());
        password.setConfirm(password.getPassword());

        // when
        put(Resources.CHANGE_PASSWORD, password);
        LoginForm login = new LoginForm();
        login.setUsername(defaultUser.getUsername());
        login.setPassword(password.getPassword());
        Response authResponse = post(Resources.USER_AUTH, login);

        // then
        assertOk(authResponse);
        Assert.assertNotNull(authResponse.readEntity(User.class).getToken());
    }
}
