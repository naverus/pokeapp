package org.ballastlane.pokeapp.infrastructure.pokeapi;

import java.net.http.HttpClient;
import org.ballastlane.pokeapp.domain.exception.CatalogUnavailableException;
import org.ballastlane.pokeapp.infrastructure.pokeapi.dto.PokeApiDtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Thin HTTP client over PokeAPI v2. Every successful response is cached. */
@Component
public class PokeApiClient {

  public static final String LIST_CACHE = "pokeapi-pokemon-list";
  public static final String POKEMON_CACHE = "pokeapi-pokemon";
  public static final String SPECIES_CACHE = "pokeapi-species";
  public static final String EVOLUTION_CACHE = "pokeapi-evolution-chain";

  private static final Logger log = LoggerFactory.getLogger(PokeApiClient.class);

  private final RestClient restClient;

  public PokeApiClient(RestClient.Builder builder, PokeApiProperties properties) {
    var httpClient =
        HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(properties.connectTimeout())
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    var requestFactory = new JdkClientHttpRequestFactory(httpClient);
    requestFactory.setReadTimeout(properties.readTimeout());
    this.restClient =
        builder.clone().baseUrl(properties.baseUrl()).requestFactory(requestFactory).build();
  }

  @Cacheable(cacheNames = LIST_CACHE, key = "#offset + ':' + #limit")
  public PokeApiDtos.PokemonList fetchPokemonList(int offset, int limit) {
    return get("/pokemon?offset=" + offset + "&limit=" + limit, PokeApiDtos.PokemonList.class);
  }

  @Cacheable(cacheNames = POKEMON_CACHE, key = "#idOrName")
  public PokeApiDtos.Pokemon fetchPokemon(String idOrName) {
    return get("/pokemon/" + idOrName, PokeApiDtos.Pokemon.class);
  }

  @Cacheable(cacheNames = SPECIES_CACHE, key = "#speciesId")
  public PokeApiDtos.Species fetchSpecies(int speciesId) {
    return get("/pokemon-species/" + speciesId, PokeApiDtos.Species.class);
  }

  @Cacheable(cacheNames = EVOLUTION_CACHE, key = "#chainId")
  public PokeApiDtos.EvolutionChain fetchEvolutionChain(int chainId) {
    return get("/evolution-chain/" + chainId, PokeApiDtos.EvolutionChain.class);
  }

  private <T> T get(String path, Class<T> type) {
    try {
      log.debug("GET PokeAPI {}", path);
      var body = restClient.get().uri(path).retrieve().body(type);
      if (body == null) {
        throw new CatalogUnavailableException("PokeAPI returned an empty body for " + path, null);
      }
      return body;
    } catch (HttpClientErrorException.NotFound ex) {
      throw new PokeApiNotFoundException(path);
    } catch (RestClientException ex) {
      log.warn("PokeAPI call {} failed: {}", path, ex.getMessage());
      throw new CatalogUnavailableException("PokeAPI is currently unavailable", ex);
    }
  }
}
