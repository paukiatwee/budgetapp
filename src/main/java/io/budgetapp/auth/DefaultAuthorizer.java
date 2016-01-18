package io.budgetapp.auth;

import io.budgetapp.model.User;
import io.dropwizard.auth.Authorizer;

/**
 *
 */
public class DefaultAuthorizer implements Authorizer<User> {
    @Override
    public boolean authorize(User user, String role) {
        return true;
    }
}