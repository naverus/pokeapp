package org.pokeapp.web.error;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;
import org.pokeapp.domain.exception.CatalogUnavailableException;
import org.pokeapp.domain.exception.DomainException;
import org.pokeapp.domain.exception.DomainValidationException;
import org.pokeapp.domain.exception.PokemonAlreadySyncedException;
import org.pokeapp.domain.exception.PokemonNotFoundException;
import org.pokeapp.domain.exception.StaleVersionException;
import org.pokeapp.domain.exception.SyncAlreadyRunningException;
import org.pokeapp.domain.exception.SyncJobNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Maps every business exception to a problem+json response. */
@Singleton
public class DomainExceptionHandler implements ExceptionHandler<DomainException, HttpResponse<?>> {

  private static final Logger log = LoggerFactory.getLogger(DomainExceptionHandler.class);

  @Override
  public HttpResponse<?> handle(HttpRequest request, DomainException ex) {
    if (ex instanceof DomainValidationException validation) {
      return Problems.validation(request, validation.errors());
    }
    if (ex instanceof PokemonNotFoundException) {
      return Problems.problem(request, HttpStatus.NOT_FOUND, "Resource not found", ex.getMessage());
    }
    if (ex instanceof SyncJobNotFoundException) {
      return Problems.problem(request, HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
    }
    if (ex instanceof PokemonAlreadySyncedException
        || ex instanceof StaleVersionException
        || ex instanceof SyncAlreadyRunningException) {
      return Problems.problem(request, HttpStatus.CONFLICT, "Conflict", ex.getMessage());
    }
    if (ex instanceof CatalogUnavailableException) {
      log.warn("Catalog unavailable: {}", ex.getMessage());
      return Problems.problem(
          request, HttpStatus.SERVICE_UNAVAILABLE, "PokeAPI unavailable", ex.getMessage());
    }
    log.error("Unhandled domain exception", ex);
    return Problems.problem(
        request,
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Internal error",
        "An unexpected error occurred");
  }
}
