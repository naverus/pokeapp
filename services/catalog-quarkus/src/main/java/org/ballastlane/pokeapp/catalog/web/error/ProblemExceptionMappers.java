package org.ballastlane.pokeapp.catalog.web.error;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.Map;
import org.ballastlane.pokeapp.catalog.domain.exception.CatalogUnavailableException;
import org.ballastlane.pokeapp.catalog.domain.exception.DomainException;
import org.ballastlane.pokeapp.catalog.domain.exception.DomainValidationException;
import org.ballastlane.pokeapp.catalog.domain.exception.PokemonNotFoundException;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

/**
 * Single place where exceptions become HTTP responses. Every error is an RFC 7807 problem detail;
 * validation errors carry an {@code errors} map of field to message.
 */
public class ProblemExceptionMappers {

  private static final Logger log = Logger.getLogger(ProblemExceptionMappers.class);
  private static final String ABOUT_BLANK = "about:blank";

  @ServerExceptionMapper(Throwable.class)
  public Response map(Throwable thrown, UriInfo uriInfo) {
    var instance = "/" + uriInfo.getPath().replaceFirst("^/", "");
    var error = unwrap(thrown);
    if (error instanceof DomainValidationException ex) {
      return problem(
          400, "Validation failed", "The request contains invalid data", instance, ex.errors());
    }
    if (error instanceof PokemonNotFoundException ex) {
      return problem(404, "Resource not found", ex.getMessage(), instance, null);
    }
    if (error instanceof CatalogUnavailableException ex) {
      log.warnf("Catalog unavailable: %s", ex.getMessage());
      return problem(503, "PokeAPI unavailable", ex.getMessage(), instance, null);
    }
    if (error instanceof WebApplicationException ex) {
      int status = ex.getResponse().getStatus();
      var reason = Response.Status.fromStatusCode(status);
      var title = reason == null ? "Error" : reason.getReasonPhrase();
      return problem(status, title, ex.getMessage(), instance, null);
    }
    log.error("Unexpected error", thrown);
    return problem(500, "Internal error", "An unexpected error occurred", instance, null);
  }

  private static Throwable unwrap(Throwable thrown) {
    var current = thrown;
    while (current != null) {
      if (current instanceof DomainException || current instanceof WebApplicationException) {
        return current;
      }
      current = current.getCause();
    }
    return thrown;
  }

  private static Response problem(
      int status,
      String title,
      String detail,
      String instance,
      Map<String, String> errors) {
    var body = new ProblemDetail(ABOUT_BLANK, title, status, detail, instance, errors);
    return Response.status(status).entity(body).type(ProblemDetail.MEDIA_TYPE).build();
  }
}
