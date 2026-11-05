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
            "must be a Pokédex number between 1 and %d".formatted(MAX_POKEAPI_ID))
        .check(
            toId >= 1 && toId <= MAX_POKEAPI_ID,
            "toId",
            "must be a Pokédex number between 1 and %d".formatted(MAX_POKEAPI_ID))
        .check(toId >= fromId, "toId", "must be at or after fromId")
        .check(
            toId - fromId < MAX_SIZE,
            "toId",
            "a restock can include at most %d Pokémon".formatted(MAX_SIZE))
        .throwIfAny();
  }

  public int size() {
    return toId - fromId + 1;
  }

  public IntStream ids() {
    return IntStream.rangeClosed(fromId, toId);
  }
}
