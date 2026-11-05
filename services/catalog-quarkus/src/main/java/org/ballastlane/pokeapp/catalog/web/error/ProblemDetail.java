package org.ballastlane.pokeapp.catalog.web.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.Map;

/** RFC 7807 body; shape matches Spring's {@code ProblemDetail} (errors flattened at top level). */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"type", "title", "status", "detail", "instance", "errors"})
public record ProblemDetail(
    String type,
    String title,
    int status,
    String detail,
    String instance,
    Map<String, String> errors) {

  public static final String MEDIA_TYPE = "application/problem+json";
}
