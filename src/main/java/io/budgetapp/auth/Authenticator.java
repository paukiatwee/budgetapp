package io.budgetapp.auth;

import io.dropwizard.auth.AuthenticationException;

import java.util.Optional;

/**
 * java 8 version of io.dropwizard.auth.Authenticator
 */
public interface Authenticator<C, P> {

    Optional<P> authenticate(C credentials) throws AuthenticationException;
}
