package org.pokeapp.web.controller;

import io.micronaut.http.HttpRequest;
import java.net.URI;

/** Builds absolute Location URIs honouring the Host and X-Forwarded-* headers. */
final class LocationUris {

  private LocationUris() {}

  static URI of(HttpRequest<?> request, String path) {
    var headers = request.getHeaders();
    var forwardedProto = first(headers.get("X-Forwarded-Proto"));
    var scheme = forwardedProto != null ? forwardedProto : request.isSecure() ? "https" : "http";
    var host = first(headers.get("X-Forwarded-Host"));
    if (host == null) {
      host = headers.get("Host");
    }
    if (host == null) {
      return URI.create(path);
    }
    return URI.create(scheme + "://" + host + path);
  }

  private static String first(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.split(",")[0].strip();
  }
}
