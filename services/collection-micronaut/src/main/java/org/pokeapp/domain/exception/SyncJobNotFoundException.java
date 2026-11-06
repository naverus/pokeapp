package org.pokeapp.domain.exception;

import java.util.UUID;

public class SyncJobNotFoundException extends DomainException {

  public SyncJobNotFoundException(UUID id) {
    super("Restock %s was not found".formatted(id));
  }
}
