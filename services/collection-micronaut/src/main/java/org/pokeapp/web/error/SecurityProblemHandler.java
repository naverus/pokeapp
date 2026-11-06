package org.pokeapp.web.error;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import io.micronaut.security.authentication.AuthorizationException;
import io.micronaut.security.authentication.DefaultAuthorizationExceptionHandler;
import jakarta.inject.Singleton;

/** Writes 401/403 responses as RFC 7807 problem details, matching the API error format. */
@Singleton
@Replaces(DefaultAuthorizationExceptionHandler.class)
public class SecurityProblemHandler
    implements ExceptionHandler<AuthorizationException, MutableHttpResponse<?>> {

  @Override
  public MutableHttpResponse<?> handle(HttpRequest request, AuthorizationException ex) {
    if (ex.isForbidden()) {
      return Problems.problem(
          request,
          HttpStatus.FORBIDDEN,
          "Forbidden",
          "You are not allowed to perform this action");
    }
    return Problems.problem(
            request,
            HttpStatus.UNAUTHORIZED,
            "Unauthorized",
            "A valid bearer token is required")
        .header("WWW-Authenticate", "Bearer");
  }
}
