package org.ballastlane.pokeapp.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import org.ballastlane.pokeapp.domain.model.Role;

public final class AuthDtos {

  private AuthDtos() {}

  public record RegisterRequest(
      @NotBlank
          @Size(min = 3, max = 30)
          @Pattern(
              regexp = "^[A-Za-z0-9_.-]+$",
              message = "may contain letters, digits, '.', '_' or '-'")
          String username,
      @NotBlank @Size(min = 8, max = 72) String password) {}

  public record LoginRequest(@NotBlank String username, @NotBlank String password) {}

  public record TokenResponse(
      String accessToken, String tokenType, Instant expiresAt, String username, Role role) {}

  public record UserResponse(Long id, String username, Role role) {}
}
