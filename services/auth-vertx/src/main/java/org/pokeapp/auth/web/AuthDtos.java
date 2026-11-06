package org.pokeapp.auth.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.pokeapp.auth.domain.DomainValidationException;
import org.pokeapp.auth.domain.Role;

/** HTTP JSON contracts (identical to the Spring monolith's {@code AuthDtos}). */
public final class AuthDtos {

  private static final Pattern USERNAME_CHARS = Pattern.compile("^[A-Za-z0-9_.-]+$");

  private AuthDtos() {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record RegisterRequest(String username, String password) {

    /** Bean-validation equivalent: @NotBlank, @Size and @Pattern constraints. */
    public void validate() {
      Map<String, String> errors = new LinkedHashMap<>();
      if (username == null || username.isBlank()) {
        errors.put("username", "must not be blank");
      } else if (username.length() < 3 || username.length() > 30) {
        errors.put("username", "size must be between 3 and 30");
      } else if (!USERNAME_CHARS.matcher(username).matches()) {
        errors.put("username", "may contain letters, digits, '.', '_' or '-'");
      }
      if (password == null || password.isBlank()) {
        errors.put("password", "must not be blank");
      } else if (password.length() < 8 || password.length() > 72) {
        errors.put("password", "size must be between 8 and 72");
      }
      if (!errors.isEmpty()) {
        throw new DomainValidationException(errors);
      }
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record LoginRequest(String username, String password) {

    public void validate() {
      Map<String, String> errors = new LinkedHashMap<>();
      if (username == null || username.isBlank()) {
        errors.put("username", "must not be blank");
      }
      if (password == null || password.isBlank()) {
        errors.put("password", "must not be blank");
      }
      if (!errors.isEmpty()) {
        throw new DomainValidationException(errors);
      }
    }
  }

  public record TokenResponse(
      String accessToken, String tokenType, Instant expiresAt, String username, Role role) {}

  public record UserResponse(Long id, String username, Role role) {}
}
