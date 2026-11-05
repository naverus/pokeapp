package org.ballastlane.pokeapp.catalog.infrastructure.pokeapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.cache.CacheKey;
import io.quarkus.cache.CacheResult;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpVersion;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.net.URI;
import org.ballastlane.pokeapp.catalog.domain.exception.CatalogUnavailableException;
import org.ballastlane.pokeapp.catalog.infrastructure.pokeapi.dto.PokeApiDtos;
import org.jboss.logging.Logger;

/**
 * Non-blocking HTTP client over PokeAPI v2 built on the Vert.x {@link WebClient}. Every successful
 * response is cached (Caffeine); failures, including 404s, are never cached.
 */
@ApplicationScoped
public class PokeApiClient {

  public static final String LIST_CACHE = "pokeapi-pokemon-list";
  public static final String POKEMON_CACHE = "pokeapi-pokemon";
  public static final String SPECIES_CACHE = "pokeapi-species";
  public static final String EVOLUTION_CACHE = "pokeapi-evolution-chain";

  private static final Logger log = Logger.getLogger(PokeApiClient.class);

  private final WebClient webClient;
  private final ObjectMapper objectMapper;
  private final String host;
  private final int port;
  private final boolean ssl;
  private final String basePath;
  private final long readTimeoutMillis;

  @Inject
  public PokeApiClient(Vertx vertx, ObjectMapper objectMapper, PokeApiProperties properties) {
    var base = URI.create(properties.baseUrl());
    this.ssl = "https".equalsIgnoreCase(base.getScheme());
    this.host = base.getHost();
    this.port = base.getPort() > 0 ? base.getPort() : (ssl ? 443 : 80);
    this.basePath = stripTrailingSlash(base.getRawPath());
    this.readTimeoutMillis = properties.readTimeout().toMillis();
    this.objectMapper = objectMapper;
    var options =
        new WebClientOptions()
            .setProtocolVersion(HttpVersion.HTTP_1_1)
            .setFollowRedirects(true)
            .setTryUseCompression(true)
            .setKeepAlive(true)
            .setUserAgent("pokeapp-catalog-quarkus")
            .setConnectTimeout((int) properties.connectTimeout().toMillis())
            .setMaxPoolSize(Math.max(1, properties.maxConcurrency()));
    this.webClient = WebClient.create(vertx, options);
  }

  @PreDestroy
  void close() {
    webClient.close();
  }

  @CacheResult(cacheName = LIST_CACHE)
  public Uni<PokeApiDtos.PokemonList> fetchPokemonList(
      @CacheKey int offset, @CacheKey int limit) {
    return get("/pokemon?offset=" + offset + "&limit=" + limit, PokeApiDtos.PokemonList.class);
  }

  @CacheResult(cacheName = POKEMON_CACHE)
  public Uni<PokeApiDtos.Pokemon> fetchPokemon(@CacheKey String idOrName) {
    return get("/pokemon/" + idOrName, PokeApiDtos.Pokemon.class);
  }

  @CacheResult(cacheName = SPECIES_CACHE)
  public Uni<PokeApiDtos.Species> fetchSpecies(@CacheKey int speciesId) {
    return get("/pokemon-species/" + speciesId, PokeApiDtos.Species.class);
  }

  @CacheResult(cacheName = EVOLUTION_CACHE)
  public Uni<PokeApiDtos.EvolutionChain> fetchEvolutionChain(@CacheKey int chainId) {
    return get("/evolution-chain/" + chainId, PokeApiDtos.EvolutionChain.class);
  }

  private <T> Uni<T> get(String path, Class<T> type) {
    log.debugf("GET PokeAPI %s", path);
    return webClient
        .get(port, host, basePath + path)
        .ssl(ssl)
        .timeout(readTimeoutMillis)
        .putHeader("Accept", "application/json")
        .send()
        .onFailure()
        .transform(
            ex -> {
              log.warnf("PokeAPI call %s failed: %s", path, ex.toString());
              return new CatalogUnavailableException("PokeAPI is currently unavailable", ex);
            })
        .map(response -> decode(path, response, type));
  }

  private <T> T decode(String path, HttpResponse<Buffer> response, Class<T> type) {
    int status = response.statusCode();
    if (status == 404) {
      throw new PokeApiNotFoundException(path);
    }
    if (status < 200 || status >= 300) {
      log.warnf("PokeAPI call %s returned HTTP %d", path, status);
      throw new CatalogUnavailableException(
          "PokeAPI is currently unavailable (HTTP %d)".formatted(status), null);
    }
    var body = response.body();
    if (body == null || body.length() == 0) {
      throw new CatalogUnavailableException("PokeAPI returned an empty body for " + path, null);
    }
    try {
      return objectMapper.readValue(body.getBytes(), type);
    } catch (IOException ex) {
      log.warnf("PokeAPI call %s returned an unreadable body: %s", path, ex.getMessage());
      throw new CatalogUnavailableException("PokeAPI returned an unreadable response", ex);
    }
  }

  private static String stripTrailingSlash(String path) {
    if (path == null || path.isEmpty()) {
      return "";
    }
    return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
  }
}
