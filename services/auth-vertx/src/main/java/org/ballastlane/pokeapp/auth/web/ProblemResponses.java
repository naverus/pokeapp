package org.ballastlane.pokeapp.auth.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.ext.web.RoutingContext;
import java.util.LinkedHashMap;
import java.util.Map;
import org.ballastlane.pokeapp.auth.domain.DomainValidationException;
import org.ballastlane.pokeapp.auth.domain.InvalidCredentialsException;
import org.ballastlane.pokeapp.auth.domain.UsernameAlreadyTakenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Single place where failures become RFC 7807 {@code application/problem+json} responses. */
public final class ProblemResponses {

  private static final Logger log = LoggerFactory.getLogger(ProblemResponses.class);
  static final String PROBLEM_JSON = "application/problem+json";

  private final ObjectMapper mapper;

  public ProblemResponses(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  public void fail(RoutingContext ctx, Throwable failure) {
    if (failure instanceof DomainValidationException e) {
      validation(ctx, e.errors());
    } else if (failure instanceof UsernameAlreadyTakenException e) {
      write(ctx, 409, "Conflict", e.getMessage(), null);
    } else if (failure instanceof InvalidCredentialsException e) {
      write(ctx, 401, "Authentication failed", e.getMessage(), null);
    } else if (failure instanceof MalformedBodyException) {
      write(ctx, 400, "Bad Request", "Failed to read request", null);
    } else {
      log.error("Unexpected error", failure);
      write(ctx, 500, "Internal error", "An unexpected error occurred", null);
    }
  }

  public void status(RoutingContext ctx, int status, String title, String detail) {
    write(ctx, status, title, detail, null);
  }

  private void validation(RoutingContext ctx, Map<String, String> errors) {
    write(ctx, 400, "Validation failed", "The request contains invalid data", errors);
  }

  private void write(
      RoutingContext ctx, int status, String title, String detail, Map<String, String> errors) {
    if (ctx.response().ended()) {
      return;
    }
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("type", "about:blank");
    body.put("title", title);
    body.put("status", status);
    body.put("detail", detail);
    body.put("instance", ctx.request().path());
    if (errors != null) {
      body.put("errors", errors);
    }
    try {
      ctx.response()
          .setStatusCode(status)
          .putHeader("Content-Type", PROBLEM_JSON)
          .end(mapper.writeValueAsString(body));
    } catch (JsonProcessingException e) {
      ctx.response().setStatusCode(500).end();
    }
  }

  /** Request body missing or not parseable as the expected JSON object. */
  public static final class MalformedBodyException extends RuntimeException {
    public MalformedBodyException(Throwable cause) {
      super("Failed to read request", cause);
    }
  }
}
