package org.ballastlane.pokeapp.auth.infrastructure;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

/** Runtime configuration read from environment variables (or same-named system properties). */
public record AppConfig(
    int httpPort,
    String jwtSecret,
    Duration jwtTtl,
    String jwtIssuer,
    DbConfig db,
    List<String> corsAllowedOrigins) {

  public record DbConfig(String host, int port, String database, String username, String password) {}

  public static AppConfig fromEnvironment() {
    return from(
        key -> {
          var value = System.getenv(key);
          return value != null ? value : System.getProperty(key);
        });
  }

  public static AppConfig from(Function<String, String> env) {
    var port = Integer.parseInt(valueOr(env, "HTTP_PORT", "8081"));
    var secret = env.apply("POKEAPP_JWT_SECRET");
    var ttl = parseDuration(valueOr(env, "POKEAPP_JWT_TTL", "2h"));
    var issuer = valueOr(env, "POKEAPP_JWT_ISSUER", "pokeapp");
    var origins =
        List.of(
            valueOr(
                    env,
                    "POKEAPP_CORS_ALLOWED_ORIGINS",
                    "http://localhost:3000,http://localhost:5173")
                .split("\\s*,\\s*"));
    var db = parseDb(env);
    return new AppConfig(port, secret, ttl, issuer, db, origins);
  }

  static DbConfig parseDb(Function<String, String> env) {
    var user = valueOr(env, "POKEAPP_DB_USERNAME", "pokeapp");
    var password = valueOr(env, "POKEAPP_DB_PASSWORD", "pokeapp");
    var url = env.apply("POKEAPP_DB_URL");
    if (url != null && !url.isBlank()) {
      return parseJdbcUrl(url.strip(), user, password);
    }
    var host = env.apply("POKEAPP_DB_HOST");
    if (host != null && !host.isBlank()) {
      return new DbConfig(
          host,
          Integer.parseInt(valueOr(env, "POKEAPP_DB_PORT", "3306")),
          valueOr(env, "POKEAPP_DB_NAME", "pokeapp"),
          user,
          password);
    }
    return new DbConfig("localhost", 3307, "pokeapp", user, password);
  }

  static DbConfig parseJdbcUrl(String url, String user, String password) {
    var raw = url.startsWith("jdbc:") ? url.substring("jdbc:".length()) : url;
    var uri = URI.create(raw);
    var host = uri.getHost() != null ? uri.getHost() : "localhost";
    var port = uri.getPort() > 0 ? uri.getPort() : 3306;
    var path = uri.getPath() == null ? "" : uri.getPath().replaceFirst("^/", "");
    var database = path.isBlank() ? "pokeapp" : path;
    return new DbConfig(host, port, database, user, password);
  }

  /** Accepts ISO-8601 ("PT2H") and Spring-style ("2h", "30m", "45s", "500ms", "1d") durations. */
  static Duration parseDuration(String text) {
    var value = text.strip().toLowerCase(Locale.ROOT);
    if (value.startsWith("p")) {
      return Duration.parse(value.toUpperCase(Locale.ROOT));
    }
    int i = 0;
    while (i < value.length() && (Character.isDigit(value.charAt(i)) || value.charAt(i) == '-')) {
      i++;
    }
    var amount = Long.parseLong(value.substring(0, i));
    return switch (value.substring(i)) {
      case "ms" -> Duration.ofMillis(amount);
      case "", "s" -> Duration.ofSeconds(amount);
      case "m" -> Duration.ofMinutes(amount);
      case "h" -> Duration.ofHours(amount);
      case "d" -> Duration.ofDays(amount);
      default -> throw new IllegalArgumentException("Unsupported duration: " + text);
    };
  }

  private static String valueOr(Function<String, String> env, String key, String fallback) {
    var value = env.apply(key);
    return value == null || value.isBlank() ? fallback : value.strip();
  }
}
