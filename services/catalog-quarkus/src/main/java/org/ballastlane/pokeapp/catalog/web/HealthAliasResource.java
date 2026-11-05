package org.ballastlane.pokeapp.catalog.web;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Map;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Spring-Actuator compatible health endpoints so nginx / docker-compose probes written for the
 * monolith keep working. The detailed Quarkus checks live under {@code /q/health}.
 */
@Path("/actuator/health")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Health")
public class HealthAliasResource {

  private static final Map<String, String> UP = Map.of("status", "UP");

  @GET
  @Operation(summary = "Simple UP status (Actuator compatible)")
  public Map<String, String> health() {
    return UP;
  }

  @GET
  @Path("/liveness")
  @Operation(summary = "Liveness (Actuator compatible)")
  public Map<String, String> liveness() {
    return UP;
  }

  @GET
  @Path("/readiness")
  @Operation(summary = "Readiness (Actuator compatible)")
  public Map<String, String> readiness() {
    return UP;
  }
}
