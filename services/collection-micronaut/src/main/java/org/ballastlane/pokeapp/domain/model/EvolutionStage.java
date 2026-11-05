package org.ballastlane.pokeapp.domain.model;

/**
 * One species in an evolution chain. {@code stage} starts at 1 for the base form; branching
 * evolutions share the same stage.
 */
public record EvolutionStage(int speciesId, String name, int stage, String spriteUrl) {}
