package io.budgetapp.auth;

import io.budgetapp.model.ValidationMessage;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import io.budgetapp.model.ValidationMessage;
import io.dropwizard.auth.Auth;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;

/**
 *
 */
public class TokenAuthProvider <T> implements InjectableProvider<Auth, Parameter> {


    private final Authenticator<Token, T> authenticator;

    public TokenAuthProvider(Authenticator<Token, T> authenticator) {
        this.authenticator = authenticator;
    }

    private static class TokenAuthInjectable<T> extends AbstractHttpContextInjectable<T> {

        private static final String HEADER_VALUE = "HMAC realm=\"%s\"";
        public static final ValidationMessage VALIDATION_MESSAGE = new ValidationMessage("error", Collections.singleton("Credentials are required to access this resource"));

        private final Authenticator<Token, T> authenticator;
        private final boolean required;

        public TokenAuthInjectable(Authenticator<Token, T> authenticator, boolean required) {
            this.authenticator = authenticator;
            this.required = required;
        }


        @Override
        public T getValue(HttpContext c) {
            // Get the Authorization header
            final String header = c.getRequest().getHeaderValue(HttpHeaders.AUTHORIZATION);
            if (!Strings.isNullOrEmpty(header)) {
                try {
                    String[] values = header.split(" ");
                    if(values.length == 2) {
                        final Optional<T> result = authenticator.authenticate(new Token(values[1]));
                        if (result.isPresent()) {
                            return result.get();
                        }
                    }

                } catch (AuthenticationException e) {
                    error();
                }
            }
            if(required) {
                error();
            }

            return null;
        }

        private void error() {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED)
                    .header(HttpHeaders.AUTHORIZATION,
                            String.format(HEADER_VALUE, "secret"))
                    .entity(VALIDATION_MESSAGE)
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .build());
        }
    }

    @Override
    public ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }

    @Override
    public Injectable getInjectable(ComponentContext componentContext, Auth auth, Parameter parameter) {
        return new TokenAuthInjectable<>(authenticator, auth.required());
    }
}
