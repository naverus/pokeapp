package org.ballastlane.pokeapp.web.error;

import java.util.LinkedHashMap;
import java.util.Map;
import org.ballastlane.pokeapp.domain.exception.CatalogUnavailableException;
import org.ballastlane.pokeapp.domain.exception.DomainException;
import org.ballastlane.pokeapp.domain.exception.DomainValidationException;
import org.ballastlane.pokeapp.domain.exception.InvalidCredentialsException;
import org.ballastlane.pokeapp.domain.exception.PokemonAlreadySyncedException;
import org.ballastlane.pokeapp.domain.exception.PokemonNotFoundException;
import org.ballastlane.pokeapp.domain.exception.StaleVersionException;
import org.ballastlane.pokeapp.domain.exception.UsernameAlreadyTakenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Single place where exceptions become HTTP responses. Every error is an RFC 7807 problem detail;
 * validation errors carry an {@code errors} map of field to message.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  private static final String ERRORS = "errors";

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    Map<String, String> errors = new LinkedHashMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
    var problem = validationProblem(errors);
    return ResponseEntity.badRequest().body(problem);
  }

  @ExceptionHandler(DomainValidationException.class)
  ProblemDetail handleDomainValidation(DomainValidationException ex) {
    return validationProblem(ex.errors());
  }

  @ExceptionHandler(PokemonNotFoundException.class)
  ProblemDetail handleNotFound(PokemonNotFoundException ex) {
    return problem(HttpStatus.NOT_FOUND, "Resource not found", ex);
  }

  @ExceptionHandler({
    PokemonAlreadySyncedException.class,
    UsernameAlreadyTakenException.class,
    StaleVersionException.class
  })
  ProblemDetail handleConflict(DomainException ex) {
    return problem(HttpStatus.CONFLICT, "Conflict", ex);
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  ProblemDetail handleInvalidCredentials(InvalidCredentialsException ex) {
    return problem(HttpStatus.UNAUTHORIZED, "Authentication failed", ex);
  }

  @ExceptionHandler(CatalogUnavailableException.class)
  ProblemDetail handleCatalogUnavailable(CatalogUnavailableException ex) {
    log.warn("Catalog unavailable: {}", ex.getMessage());
    return problem(HttpStatus.SERVICE_UNAVAILABLE, "PokeAPI unavailable", ex);
  }

  @ExceptionHandler(Exception.class)
  ProblemDetail handleUnexpected(Exception ex) {
    log.error("Unexpected error", ex);
    var problem =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    problem.setTitle("Internal error");
    return problem;
  }

  private static ProblemDetail validationProblem(Map<String, String> errors) {
    var problem =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, "The request contains invalid data");
    problem.setTitle("Validation failed");
    problem.setProperty(ERRORS, errors);
    return problem;
  }

  private static ProblemDetail problem(HttpStatus status, String title, Exception ex) {
    var problem = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
    problem.setTitle(title);
    return problem;
  }
}
