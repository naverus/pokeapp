package org.ballastlane.pokeapp.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.ballastlane.pokeapp.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UserTest {

  @Test
  void shouldCreateNewUserWithUserRole_whenRegistering() {
    var user = User.newUser("  Ash_Ketchum ", "hash");

    assertThat(user.id()).isNull();
    assertThat(user.username()).isEqualTo("ash_ketchum");
    assertThat(user.role()).isEqualTo(Role.USER);
  }

  @ParameterizedTest
  @ValueSource(strings = {"ab", "this-username-is-way-too-long-for-us", "bad name", "x$y"})
  void shouldRejectInvalidUsername(String username) {
    assertThatThrownBy(() -> User.newUser(username, "hash"))
        .isInstanceOfSatisfying(
            DomainValidationException.class, ex -> assertThat(ex.errors()).containsKey("username"));
  }

  @Test
  void shouldAcceptPasswordWithinPolicy() {
    assertThatCode(() -> PasswordPolicy.validate("pikachu123")).doesNotThrowAnyException();
  }

  @ParameterizedTest
  @ValueSource(strings = {"short1", "onlyletters", "12345678"})
  void shouldRejectPasswordOutsidePolicy(String password) {
    assertThatThrownBy(() -> PasswordPolicy.validate(password))
        .isInstanceOfSatisfying(
            DomainValidationException.class, ex -> assertThat(ex.errors()).containsKey("password"));
  }

  @Test
  void shouldRejectNullOrTooLongPassword() {
    assertThatThrownBy(() -> PasswordPolicy.validate(null))
        .isInstanceOf(DomainValidationException.class);
    assertThatThrownBy(() -> PasswordPolicy.validate("a1".repeat(37)))
        .isInstanceOf(DomainValidationException.class);
  }
}
