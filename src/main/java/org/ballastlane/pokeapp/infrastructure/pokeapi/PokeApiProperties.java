package org.ballastlane.pokeapp.infrastructure.pokeapi;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("pokeapp.pokeapi")
public record PokeApiProperties(
    @DefaultValue("https://pokeapi.co/api/v2") String baseUrl,
    @DefaultValue("3s") Duration connectTimeout,
    @DefaultValue("10s") Duration readTimeout,
    @DefaultValue("16") int maxConcurrency) {}
