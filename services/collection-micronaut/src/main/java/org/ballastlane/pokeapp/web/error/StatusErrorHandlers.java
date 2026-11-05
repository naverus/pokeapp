package org.ballastlane.pokeapp.web.error;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Error;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;

/** Problem+json bodies for router level errors (unknown route, wrong method, bad media type). */
@Controller
@Secured(SecurityRule.IS_ANONYMOUS)
public class StatusErrorHandlers {

  @Error(global = true, status = HttpStatus.NOT_FOUND)
  public HttpResponse<?> notFound(HttpRequest<?> request) {
    return Problems.problem(
        request, HttpStatus.NOT_FOUND, "Not Found", "No resource at " + request.getPath());
  }

  @Error(global = true, status = HttpStatus.METHOD_NOT_ALLOWED)
  public HttpResponse<?> methodNotAllowed(HttpRequest<?> request) {
    return Problems.problem(
        request,
        HttpStatus.METHOD_NOT_ALLOWED,
        "Method Not Allowed",
        "Method %s is not supported for this resource".formatted(request.getMethodName()));
  }

  @Error(global = true, status = HttpStatus.UNSUPPORTED_MEDIA_TYPE)
  public HttpResponse<?> unsupportedMediaType(HttpRequest<?> request) {
    return Problems.problem(
        request,
        HttpStatus.UNSUPPORTED_MEDIA_TYPE,
        "Unsupported Media Type",
        "Content type is not supported");
  }

  @Error(global = true, status = HttpStatus.BAD_REQUEST)
  public HttpResponse<?> badRequest(HttpRequest<?> request) {
    return Problems.problem(request, HttpStatus.BAD_REQUEST, "Bad Request", "Malformed request");
  }
}
