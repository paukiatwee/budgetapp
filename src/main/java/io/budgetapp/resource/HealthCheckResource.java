package io.budgetapp.resource;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

/**
 *
 */
@Path(ResourceURL.HEALTH)
@Produces(MediaType.APPLICATION_JSON)
public class HealthCheckResource extends AbstractResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheckResource.class);

    private final HealthCheckRegistry healthCheckRegistry;

    public HealthCheckResource(HealthCheckRegistry healthCheckRegistry) {
        this.healthCheckRegistry = healthCheckRegistry;
    }

    @Override
    public String getPath() {
        return ResourceURL.HEALTH;
    }

    @GET
    @Path("/ping")
    public Response ping() {
        try {
            Map<String, HealthCheck.Result> results = healthCheckRegistry.runHealthChecks();

            for (Map.Entry<String, HealthCheck.Result> entry : results.entrySet()) {
                if (!entry.getValue().isHealthy()) {
                    LOGGER.info(entry.getValue().getMessage(), entry.getValue().getError());
                    return error("error");
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return error("error");
        }

        return ok("ok");
    }
}
