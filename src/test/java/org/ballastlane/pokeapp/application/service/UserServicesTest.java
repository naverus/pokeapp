package org.ballastlane.pokeapp.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.Optional;
import org.ballastlane.pokeapp.application.port.in.AuthenticateUserUseCase.LoginCommand;
import org.ballastlane.pokeapp.application.port.in.RegisterUserUseCase.RegisterUserCommand;
import org.ballastlane.pokeapp.application.port.out.PasswordHasherPort;
import org.ballastlane.pokeapp.application.port.out.TokenIssuerPort;
import org.ballastlane.pokeapp.application.port.out.TokenIssuerPort.IssuedToken;
import org.ballastlane.pokeapp.application.port.out.UserRepositoryPort;
import org.ballastlane.pokeapp.domain.exception.DomainValidationException;
import org.ballastlane.pokeapp.domain.exception.InvalidCredentialsException;
import org.ballastlane.pokeapp.domain.exception.UsernameAlreadyTakenException;
import org.ballastlane.pokeapp.domain.model.Role;
import org.ballastlane.pokeapp.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServicesTest {

  private static final User ASH = new User(1L, "ash", "hashed", Role.USER, Instant.EPOCH);

  @Mock private UserRepositoryPort users;
  @Mock private PasswordHasherPort hasher;
  @Mock private TokenIssuerPort tokens;

  @Test
  void shouldRegisterUserWithHashedPassword() {
    // given
    given(users.existsByUsername("ash")).willReturn(false);
    given(hasher.hash("pikachu123")).willReturn("hashed");
    given(users.save(any())).willAnswer(invocation -> invocation.getArgument(0));

    // when
    var user =
        new RegisterUserService(users, hasher)
            .register(new RegisterUserCommand(" Ash ", "pikachu123"));

    // then
    assertThat(user.username()).isEqualTo("ash");
    assertThat(user.passwordHash()).isEqualTo("hashed");
    assertThat(user.role()).isEqualTo(Role.USER);
  }

  @Test
  void shouldRejectRegistration_whenUsernameTaken() {
    given(users.existsByUsername("ash")).willReturn(true);

    assertThatThrownBy(
            () ->
                new RegisterUserService(users, hasher)
                    .register(new RegisterUserCommand("ash", "pikachu123")))
        .isInstanceOf(UsernameAlreadyTakenException.class);
    verify(users, never()).save(any());
  }

  @Test
  void shouldRejectRegistration_whenPasswordIsWeak() {
    assertThatThrownBy(
            () ->
                new RegisterUserService(users, hasher)
                    .register(new RegisterUserCommand("ash", "weak")))
        .isInstanceOf(DomainValidationException.class);
    verify(hasher, never()).hash(any());
  }

  @Test
  void shouldIssueToken_whenCredentialsMatch() {
    // given
    var expiresAt = Instant.parse("2026-01-01T01:00:00Z");
    given(users.findByUsername("ash")).willReturn(Optional.of(ASH));
    given(hasher.matches("pikachu123", "hashed")).willReturn(true);
    given(tokens.issue(ASH)).willReturn(new IssuedToken("jwt", expiresAt));

    // when
    var result =
        new AuthenticateUserService(users, hasher, tokens)
            .authenticate(new LoginCommand("ASH", "pikachu123"));

    // then
    assertThat(result.accessToken()).isEqualTo("jwt");
    assertThat(result.expiresAt()).isEqualTo(expiresAt);
    assertThat(result.username()).isEqualTo("ash");
    assertThat(result.role()).isEqualTo(Role.USER);
  }

  @Test
  void shouldRejectLogin_whenPasswordDoesNotMatch() {
    given(users.findByUsername("ash")).willReturn(Optional.of(ASH));
    given(hasher.matches("wrong", "hashed")).willReturn(false);

    assertThatThrownBy(
            () ->
                new AuthenticateUserService(users, hasher, tokens)
                    .authenticate(new LoginCommand("ash", "wrong")))
        .isInstanceOf(InvalidCredentialsException.class);
  }

  @Test
  void shouldRejectLogin_whenUserUnknown() {
    given(users.findByUsername("misty")).willReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                new AuthenticateUserService(users, hasher, tokens)
                    .authenticate(new LoginCommand("misty", "pikachu123")))
        .isInstanceOf(InvalidCredentialsException.class);
  }

  @Test
  void shouldRejectLogin_whenCredentialsMissing() {
    assertThatThrownBy(
            () ->
                new AuthenticateUserService(users, hasher, tokens)
                    .authenticate(new LoginCommand(null, null)))
        .isInstanceOf(InvalidCredentialsException.class);
  }
}
