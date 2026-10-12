package org.ballastlane.pokeapp.domain.model;

import java.util.stream.IntStream;

/** Inclusive range of PokeAPI ids to replicate in one synchronization job. */
public record SyncRange(int fromId, int toId) {

  public static final int MAX_POKEAPI_ID = 1025;
  public static final int MAX_SIZE = 500;
  public static final SyncRange DEFAULT = new SyncRange(1, 151);

  public SyncRange {
    new ValidationErrors()
        .check(
            fromId >= 1 && fromId <= MAX_POKEAPI_ID,
            "fromId",
            "must be between 1 and %d".formatted(MAX_POKEAPI_ID))
        .check(
            toId >= 1 && toId <= MAX_POKEAPI_ID,
            "toId",
            "must be between 1 and %d".formatted(MAX_POKEAPI_ID))
        .check(toId >= fromId, "toId", "must be greater than or equal to fromId")
        .check(
            toId - fromId < MAX_SIZE,
            "toId",
            "range must contain at most %d Pokemon".formatted(MAX_SIZE))
        .throwIfAny();
  }

  public int size() {
    return toId - fromId + 1;
  }

  public IntStream ids() {
    return IntStream.rangeClosed(fromId, toId);
  }
}
