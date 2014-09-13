package io.budgetapp.auth;

import io.budgetapp.model.User;
import io.budgetapp.service.FinanceService;
import io.dropwizard.auth.AuthenticationException;

import java.util.Optional;

/**
 *
 */
public class TokenAuthenticator implements Authenticator<Token, User> {

    private final FinanceService financeService;

    public TokenAuthenticator(FinanceService financeService) {
        this.financeService = financeService;
    }

    @Override
    public Optional<User> authenticate(Token token) throws AuthenticationException {
        return financeService.findUserByToken(token.getValue());
    }
}
