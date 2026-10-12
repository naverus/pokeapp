package org.ballastlane.pokeapp.infrastructure.persistence;

import java.util.List;
import org.ballastlane.pokeapp.domain.model.LocalPokemon;
import org.ballastlane.pokeapp.domain.model.PokemonAttributes;
import org.ballastlane.pokeapp.infrastructure.persistence.entity.PokemonJpaEntity;

final class PokemonEntityMapper {

  private PokemonEntityMapper() {}

  static LocalPokemon toDomain(PokemonJpaEntity entity) {
    var attributes =
        new PokemonAttributes(
            entity.getName(),
            entity.getLocalName(),
            entity.getRegion(),
            entity.getCategory(),
            entity.getHeightM(),
            entity.getWeightKg(),
            entity.getSpriteUrl(),
            entity.getDescription(),
            List.copyOf(entity.getTypes()),
            List.copyOf(entity.getAbilities()),
            List.copyOf(entity.getTags()));
    return new LocalPokemon(
        entity.getId(),
        entity.getPokeApiId(),
        attributes,
        entity.getVersion() == null ? 0L : entity.getVersion(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }

  static void copyAttributes(PokemonAttributes attributes, PokemonJpaEntity entity) {
    entity.setName(attributes.name());
    entity.setLocalName(attributes.localName());
    entity.setRegion(attributes.region());
    entity.setCategory(attributes.category());
    entity.setHeightM(attributes.heightM());
    entity.setWeightKg(attributes.weightKg());
    entity.setSpriteUrl(attributes.spriteUrl());
    entity.setDescription(attributes.description());
    entity.replaceTypes(attributes.types());
    entity.replaceAbilities(attributes.abilities());
    entity.replaceTags(attributes.tags());
  }
}
