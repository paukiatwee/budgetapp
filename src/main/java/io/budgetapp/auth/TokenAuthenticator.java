package io.budgetapp.auth;

import com.google.common.base.Optional;
import io.budgetapp.model.User;
import io.budgetapp.service.FinanceService;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.hibernate.UnitOfWork;

/**
 *
 */
public class TokenAuthenticator implements Authenticator<String, User> {

    private final FinanceService financeService;

    public TokenAuthenticator(FinanceService financeService) {
        this.financeService = financeService;
    }

    @UnitOfWork
    @Override
    public Optional<User> authenticate(String token) throws AuthenticationException {
        java.util.Optional<User> option = financeService.findUserByToken(token);
        if(option.isPresent()) {
            return Optional.of(option.get());
        } else {
            return Optional.absent();
        }
    }
}
