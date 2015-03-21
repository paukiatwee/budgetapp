package io.budgetapp.resource;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 */
abstract class AbstractResource {

    public abstract String getPath();

    public Response ok(Object object) {
        return Response.ok(object).build();
    }

    public Response ok() {
        return Response.ok().build();
    }

    public Response bad(Object object) {
        return Response.status(Response.Status.BAD_REQUEST).entity(object).build();
    }

    public Response bad() {
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    public Response error() {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    public Response error(String message) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(message).build();
    }

    public Response created(Object object, Object id) {

        try {
            return Response.created(new URI(getPath() + "/" + String.valueOf(id))).entity(object).build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Response notFound() {
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    public Response deleted() {
        return Response.noContent().build();
    }

    public Response unauthorized() {
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }
}
