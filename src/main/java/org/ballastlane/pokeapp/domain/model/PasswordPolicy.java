package org.ballastlane.pokeapp.domain.model;

public final class PasswordPolicy {

  public static final int MIN_LENGTH = 8;
  public static final int MAX_LENGTH = 72;

  private PasswordPolicy() {}

  public static void validate(String rawPassword) {
    ValidationErrors.require(
        rawPassword != null
            && rawPassword.length() >= MIN_LENGTH
            && rawPassword.length() <= MAX_LENGTH
            && rawPassword.chars().anyMatch(Character::isLetter)
            && rawPassword.chars().anyMatch(Character::isDigit),
        "password",
        "must be %d-%d characters and contain at least one letter and one digit"
            .formatted(MIN_LENGTH, MAX_LENGTH));
  }
}
