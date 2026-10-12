package org.ballastlane.pokeapp.fixtures;

import java.time.Instant;
import java.util.List;
import org.ballastlane.pokeapp.domain.model.EvolutionStage;
import org.ballastlane.pokeapp.domain.model.LocalPokemon;
import org.ballastlane.pokeapp.domain.model.PokemonAttributes;
import org.ballastlane.pokeapp.domain.model.PokemonDetail;
import org.ballastlane.pokeapp.domain.model.PokemonSummary;
import org.ballastlane.pokeapp.domain.model.Stat;

public final class PokemonFixtures {

  public static final String PIKACHU_IMAGE =
      "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/25.png";

  private PokemonFixtures() {}

  public static PokemonSummary pikachuSummary() {
    return new PokemonSummary(
        25,
        "pikachu",
        PIKACHU_IMAGE,
        "Mouse Pokemon",
        6.0,
        List.of("static", "lightning-rod"),
        List.of("electric"));
  }

  public static PokemonDetail pikachuDetail() {
    return new PokemonDetail(
        25,
        "pikachu",
        PIKACHU_IMAGE,
        "Mouse Pokemon",
        0.4,
        6.0,
        List.of("electric"),
        List.of("static", "lightning-rod"),
        List.of(new Stat("hp", 35), new Stat("attack", 55)),
        "When several of these Pokemon gather, their electricity could build and cause lightning storms.",
        List.of(
            new EvolutionStage(172, "pichu", 1, "pichu.png"),
            new EvolutionStage(25, "pikachu", 2, "pikachu.png"),
            new EvolutionStage(26, "raichu", 3, "raichu.png")));
  }

  public static PokemonAttributes pikachuAttributes() {
    return new PokemonAttributes(
        "pikachu",
        null,
        "Kanto",
        "Mouse Pokemon",
        0.4,
        6.0,
        PIKACHU_IMAGE,
        "Electric mouse",
        List.of("electric"),
        List.of("static", "lightning-rod"),
        List.of("starter"));
  }

  public static LocalPokemon storedPikachu() {
    var timestamp = Instant.parse("2026-01-01T00:00:00Z");
    return new LocalPokemon(1L, 25, pikachuAttributes(), 0L, timestamp, timestamp);
  }
}
