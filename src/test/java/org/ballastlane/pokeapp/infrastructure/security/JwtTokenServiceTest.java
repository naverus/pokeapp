package org.ballastlane.pokeapp.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.ballastlane.pokeapp.domain.model.Role;
import org.ballastlane.pokeapp.domain.model.User;
import org.junit.jupiter.api.Test;

class JwtTokenServiceTest {

  private static final String SECRET = "a-test-secret-that-is-definitely-longer-than-32-bytes";
  private static final Instant NOW = Instant.parse("2026-10-12T12:00:00Z");
  private static final User ADMIN = new User(1L, "admin", "hash", Role.ADMIN, NOW);

  private static JwtTokenService serviceAt(Instant instant, String secret) {
    return new JwtTokenService(
        new SecurityProperties(secret, Duration.ofHours(2), "pokeapp"),
        Clock.fixed(instant, ZoneOffset.UTC));
  }

  @Test
  void shouldRoundTripUsernameAndRole() {
    // given
    var service = serviceAt(NOW, SECRET);

    // when
    var token = service.issue(ADMIN);

    // then
    assertThat(token.expiresAt()).isEqualTo(NOW.plus(Duration.ofHours(2)));
    assertThat(service.verify(token.value())).contains(new AuthenticatedUser("admin", Role.ADMIN));
  }

  @Test
  void shouldRejectExpiredToken() {
    var token = serviceAt(NOW, SECRET).issue(ADMIN);

    assertThat(serviceAt(NOW.plus(Duration.ofHours(3)), SECRET).verify(token.value())).isEmpty();
  }

  @Test
  void shouldRejectTokenSignedWithAnotherKey() {
    var token = serviceAt(NOW, "another-secret-that-is-also-longer-than-32-bytes!!").issue(ADMIN);

    assertThat(serviceAt(NOW, SECRET).verify(token.value())).isEmpty();
  }

  @Test
  void shouldRejectGarbage() {
    assertThat(serviceAt(NOW, SECRET).verify("not-a-jwt")).isEmpty();
  }

  @Test
  void shouldRefuseToStart_whenSecretTooShort() {
    assertThatThrownBy(() -> serviceAt(NOW, "short")).isInstanceOf(IllegalStateException.class);
    assertThatThrownBy(() -> serviceAt(NOW, null)).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void shouldHashAndMatchPasswords() {
    var hasher = new BCryptPasswordHasher();
    var hash = hasher.hash("pikachu123");

    assertThat(hash).isNotEqualTo("pikachu123");
    assertThat(hasher.matches("pikachu123", hash)).isTrue();
    assertThat(hasher.matches("raichu123", hash)).isFalse();
  }
}
