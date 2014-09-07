package io.budgetapp.client;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

import javax.ws.rs.core.HttpHeaders;

/**
 *
 */
public class HTTPTokenClientFilter extends ClientFilter {

    private final String token;

    public HTTPTokenClientFilter(String token) {
        this.token = token;
    }

    @Override
    public ClientResponse handle(ClientRequest cr) throws ClientHandlerException {
        cr.getHeaders().add(HttpHeaders.AUTHORIZATION, "Basic " + token);
        return getNext().handle(cr);
    }
}
