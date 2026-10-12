package org.ballastlane.pokeapp.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.time.Clock;
import org.ballastlane.pokeapp.infrastructure.pokeapi.PokeApiProperties;
import org.ballastlane.pokeapp.infrastructure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableCaching
@EnableConfigurationProperties({PokeApiProperties.class, SecurityProperties.class})
public class ApplicationConfig {

  static final String BEARER_SCHEME = "bearerAuth";

  @Bean
  Clock clock() {
    return Clock.systemUTC();
  }

  @Bean
  OpenAPI pokeAppOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("PokeApp API")
                .version("v1")
                .description(
                    "Browse PokeAPI, replicate Pokemon locally and enrich them with custom fields."))
        .components(
            new Components()
                .addSecuritySchemes(
                    BEARER_SCHEME,
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
  }
}
