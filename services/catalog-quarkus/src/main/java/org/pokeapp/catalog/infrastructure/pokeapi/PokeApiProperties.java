package org.pokeapp.catalog.infrastructure.pokeapi;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import java.time.Duration;

@ConfigMapping(prefix = "pokeapp.pokeapi")
public interface PokeApiProperties {

  @WithDefault("https://pokeapi.co/api/v2")
  String baseUrl();

  @WithDefault("3s")
  Duration connectTimeout();

  @WithDefault("10s")
  Duration readTimeout();

  @WithDefault("16")
  int maxConcurrency();
}
