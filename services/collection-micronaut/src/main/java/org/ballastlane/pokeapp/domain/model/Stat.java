package org.ballastlane.pokeapp.domain.model;

public record Stat(String name, int baseValue) {

  public Stat {
    ValidationErrors.require(baseValue >= 0, "baseValue", "must be zero or positive");
  }
}
