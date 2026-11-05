package org.ballastlane.pokeapp.domain.model;

public record PageQuery(int page, int size) {

  public static final int MAX_SIZE = 100;

  public PageQuery {
    new ValidationErrors()
        .check(page >= 0, "page", "must be zero or positive")
        .check(size >= 1 && size <= MAX_SIZE, "size", "must be between 1 and " + MAX_SIZE)
        .throwIfAny();
  }

  public int offset() {
    return page * size;
  }
}
