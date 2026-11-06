package org.pokeapp.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import org.pokeapp.auth.domain.DomainValidationException;
import org.pokeapp.auth.domain.PasswordPolicy;
import org.pokeapp.auth.domain.Role;
import org.pokeapp.auth.domain.User;
import org.pokeapp.auth.infrastructure.AppConfig;
import org.pokeapp.auth.infrastructure.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

class AuthCoreTest {

  private static final String SECRET = "0123456789abcdef0123456789abcdef";

  @Test
  void usernameIsNormalizedAndValidated() {
    assertEquals("ash", User.newUser("  ASH ", "h").username());
    assertThrows(DomainValidationException.class, () -> User.newUser("a", "h"));
    assertThrows(DomainValidationException.class, () -> User.newUser("bad name", "h"));
  }

  @Test
  void passwordPolicy() {
    PasswordPolicy.validate("pikachu123");
    assertThrows(DomainValidationException.class, () -> PasswordPolicy.validate("short1"));
    assertThrows(DomainValidationException.class, () -> PasswordPolicy.validate("onlyletters"));
    assertThrows(DomainValidationException.class, () -> PasswordPolicy.validate("12345678"));
  }

  @Test
  void bcryptVerifiesSpringSeededHashes() {
    // Hash from the Flyway seed (Spring BCryptPasswordEncoder, $2a$10$)
    var ash = "$2a$10$iUgg7ye7Fd9szCBRvwdaq.gWiW99XZkMOM4J92aRkm9RQ5doatYFW";
    assertTrue(BCrypt.checkpw("pikachu123", ash));
    assertFalse(BCrypt.checkpw("wrong", ash));
  }

  @Test
  void jwtMatchesContract() {
    var service = new JwtTokenService(SECRET, "pokeapp", Duration.ofHours(2), Clock.systemUTC());
    var issued = service.issue(new User(1L, "ash", "h", Role.USER));
    var claims =
        Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
            .requireIssuer("pokeapp")
            .build()
            .parseSignedClaims(issued.value());
    assertEquals("HS256", claims.getHeader().getAlgorithm());
    assertEquals("ash", claims.getPayload().getSubject());
    assertEquals("USER", claims.getPayload().get("role", String.class));
  }

  @Test
  void shortSecretIsRejected() {
    assertThrows(
        IllegalStateException.class,
        () -> new JwtTokenService("short", "pokeapp", Duration.ofHours(2), Clock.systemUTC()));
  }

  @Test
  void configParsing() {
    var cfg = AppConfig.from(Map.of("POKEAPP_DB_URL", "jdbc:mysql://mysql:3306/pokeapp?x=1")::get);
    assertEquals("mysql", cfg.db().host());
    assertEquals(3306, cfg.db().port());
    assertEquals("pokeapp", cfg.db().database());
    assertEquals(8081, cfg.httpPort());
    assertEquals(Duration.ofHours(2), cfg.jwtTtl());
    var defaults = AppConfig.from(k -> null);
    assertEquals(3307, defaults.db().port());
    assertEquals(Duration.ofMinutes(30), AppConfig.from(Map.of("POKEAPP_JWT_TTL", "30m")::get).jwtTtl());
  }
}
