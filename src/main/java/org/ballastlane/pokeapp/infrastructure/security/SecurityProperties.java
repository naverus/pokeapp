package org.ballastlane.pokeapp.infrastructure.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("pokeapp.security")
public record SecurityProperties(
    String jwtSecret,
    @DefaultValue("2h") Duration tokenTtl,
    @DefaultValue("pokeapp") String issuer) {}
