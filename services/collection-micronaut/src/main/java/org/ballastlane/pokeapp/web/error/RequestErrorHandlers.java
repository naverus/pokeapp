package org.ballastlane.pokeapp.web.error;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.core.convert.exceptions.ConversionErrorException;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.codec.CodecException;
import io.micronaut.http.server.exceptions.ConversionErrorHandler;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import io.micronaut.http.server.exceptions.JacksonExceptionHandler;
import io.micronaut.http.server.exceptions.JsonExceptionHandler;
import io.micronaut.json.JsonSyntaxException;
import io.micronaut.web.router.exceptions.UnsatisfiedRouteException;
import io.micronaut.serde.exceptions.SerdeException;
import io.micronaut.validation.exceptions.ConstraintExceptionHandler;
import jakarta.inject.Singleton;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Translates framework level request errors into problem+json. */
public final class RequestErrorHandlers {

  private RequestErrorHandlers() {}

  @Singleton
  @Replaces(ConstraintExceptionHandler.class)
  public static class ValidationHandler
      implements ExceptionHandler<ConstraintViolationException, HttpResponse<?>> {

    @Override
    public HttpResponse<?> handle(HttpRequest request, ConstraintViolationException ex) {
      Map<String, String> errors = new LinkedHashMap<>();
      for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
        errors.putIfAbsent(fieldOf(violation.getPropertyPath()), violation.getMessage());
      }
      return Problems.validation(request, errors);
    }

    private static String fieldOf(Path path) {
      var segments = new java.util.ArrayList<String>();
      for (Path.Node node : path) {
        if (node.getName() != null) {
          segments.add(node.getName());
        }
      }
      // Drop the method and argument names ("update.body.name" -> "name").
      var skip = Math.min(2, Math.max(0, segments.size() - 1));
      return String.join(".", segments.subList(skip, segments.size()));
    }
  }

  @Singleton
  @Replaces(ConversionErrorHandler.class)
  public static class ConversionHandler
      implements ExceptionHandler<ConversionErrorException, HttpResponse<?>> {

    @Override
    public HttpResponse<?> handle(HttpRequest request, ConversionErrorException ex) {
      var errors = Map.of(ex.getArgument().getName(), "has an invalid value");
      return Problems.problem(
          request,
          HttpStatus.BAD_REQUEST,
          "Bad Request",
          "Invalid value for '%s'".formatted(ex.getArgument().getName()),
          errors);
    }
  }

  @Singleton
  @Replaces(io.micronaut.http.server.exceptions.UnsatisfiedRouteHandler.class)
  public static class UnsatisfiedRouteProblemHandler
      implements ExceptionHandler<UnsatisfiedRouteException, HttpResponse<?>> {

    @Override
    public HttpResponse<?> handle(HttpRequest request, UnsatisfiedRouteException ex) {
      return Problems.problem(
          request,
          HttpStatus.BAD_REQUEST,
          "Bad Request",
          "Required argument '%s' is missing".formatted(ex.getArgument().getName()));
    }
  }

  @Singleton
  @Replaces(JacksonExceptionHandler.class)
  public static class JacksonHandler
      implements ExceptionHandler<com.fasterxml.jackson.core.JsonProcessingException, HttpResponse<?>> {

    @Override
    public HttpResponse<?> handle(
        HttpRequest request, com.fasterxml.jackson.core.JsonProcessingException ex) {
      return Problems.problem(
          request, HttpStatus.BAD_REQUEST, "Bad Request", "Request body is not valid JSON");
    }
  }

  @Singleton
  @Replaces(JsonExceptionHandler.class)
  public static class JsonSyntaxHandler
      implements ExceptionHandler<JsonSyntaxException, HttpResponse<?>> {

    @Override
    public HttpResponse<?> handle(HttpRequest request, JsonSyntaxException ex) {
      return Problems.problem(
          request, HttpStatus.BAD_REQUEST, "Bad Request", "Request body is not valid JSON");
    }
  }

  @Singleton
  public static class SerdeHandler implements ExceptionHandler<SerdeException, HttpResponse<?>> {

    @Override
    public HttpResponse<?> handle(HttpRequest request, SerdeException ex) {
      return Problems.problem(
          request, HttpStatus.BAD_REQUEST, "Bad Request", "Failed to read request body");
    }
  }

  @Singleton
  public static class CodecHandler implements ExceptionHandler<CodecException, HttpResponse<?>> {

    @Override
    public HttpResponse<?> handle(HttpRequest request, CodecException ex) {
      return Problems.problem(
          request, HttpStatus.BAD_REQUEST, "Bad Request", "Failed to read request body");
    }
  }

  @Singleton
  public static class UnexpectedHandler implements ExceptionHandler<RuntimeException, HttpResponse<?>> {

    private static final Logger log = LoggerFactory.getLogger(UnexpectedHandler.class);

    @Override
    public HttpResponse<?> handle(HttpRequest request, RuntimeException ex) {
      log.error("Unexpected error", ex);
      return Problems.problem(
          request,
          HttpStatus.INTERNAL_SERVER_ERROR,
          "Internal error",
          "An unexpected error occurred");
    }
  }
}
