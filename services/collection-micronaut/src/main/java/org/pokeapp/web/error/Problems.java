package org.pokeapp.web.error;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;

/** Builds RFC 7807 {@code application/problem+json} responses shaped like Spring's ProblemDetail. */
public final class Problems {

  public static final MediaType PROBLEM_JSON = new MediaType("application/problem+json");

  private Problems() {}

  public static MutableHttpResponse<Map<String, Object>> problem(
      HttpRequest<?> request, HttpStatus status, String title, String detail) {
    return problem(request, status, title, detail, null);
  }

  public static MutableHttpResponse<Map<String, Object>> validation(
      HttpRequest<?> request, Map<String, String> errors) {
    return problem(
        request,
        HttpStatus.BAD_REQUEST,
        "Validation failed",
        "The request contains invalid data",
        errors);
  }

  public static MutableHttpResponse<Map<String, Object>> problem(
      HttpRequest<?> request,
      HttpStatus status,
      String title,
      String detail,
      Map<String, String> errors) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("type", "about:blank");
    body.put("title", title);
    body.put("status", status.getCode());
    body.put("detail", detail);
    body.put("instance", request.getPath());
    if (errors != null) {
      body.put("errors", errors);
    }
    return HttpResponse.<Map<String, Object>>status(status).body(body).contentType(PROBLEM_JSON);
  }
}
