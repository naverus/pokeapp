package org.ballastlane.pokeapp.domain.model;

import java.util.Locale;
import java.util.regex.Pattern;

/** A PokeAPI id (positive number) or a lower-case name such as {@code mr-mime}. */
public record PokemonIdentifier(String value) {

  private static final Pattern VALID = Pattern.compile("^(?:[1-9]\\d{0,5}|[a-z][a-z0-9-]{0,49})$");

  public PokemonIdentifier {
    ValidationErrors.require(
        value != null && VALID.matcher(value).matches(),
        "idOrName",
        "must be a positive id or a name made of letters, digits and '-'");
  }

  public static PokemonIdentifier of(String raw) {
    return new PokemonIdentifier(raw == null ? null : raw.strip().toLowerCase(Locale.ROOT));
  }
}
