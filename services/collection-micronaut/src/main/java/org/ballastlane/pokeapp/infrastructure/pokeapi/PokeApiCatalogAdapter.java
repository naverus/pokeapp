package org.ballastlane.pokeapp.infrastructure.pokeapi;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.exceptions.HttpClientException;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import jakarta.inject.Singleton;
import java.util.Optional;
import org.ballastlane.pokeapp.application.port.out.PokemonCatalogPort;
import org.ballastlane.pokeapp.domain.exception.CatalogUnavailableException;
import org.ballastlane.pokeapp.domain.model.PokemonDetail;
import org.ballastlane.pokeapp.infrastructure.pokeapi.dto.PokeApiDtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Implements the catalog port on top of PokeAPI (only the detail lookup used by sync). */
@Singleton
public class PokeApiCatalogAdapter implements PokemonCatalogPort {

  private static final Logger log = LoggerFactory.getLogger(PokeApiCatalogAdapter.class);

  private final PokeApiHttpClient client;

  public PokeApiCatalogAdapter(PokeApiHttpClient client) {
    this.client = client;
  }

  @Override
  public Optional<PokemonDetail> findDetail(String idOrName) {
    try {
      var pokemon = client.fetchPokemon(idOrName);
      if (pokemon.isEmpty()) {
        return Optional.empty();
      }
      var species = client.fetchSpecies(PokeApiMapper.idFromUrl(pokemon.get().species().url()));
      if (species.isEmpty()) {
        return Optional.empty();
      }
      var chainRef = species.get().evolutionChain();
      PokeApiDtos.EvolutionChain chain = null;
      if (chainRef != null) {
        chain = client.fetchEvolutionChain(PokeApiMapper.idFromUrl(chainRef.url())).orElse(null);
      }
      return Optional.of(PokeApiMapper.toDetail(pokemon.get(), species.get(), chain));
    } catch (HttpClientResponseException ex) {
      if (ex.getStatus() == HttpStatus.NOT_FOUND) {
        return Optional.empty();
      }
      log.warn("PokeAPI lookup of '{}' failed: {}", idOrName, ex.getMessage());
      throw new CatalogUnavailableException("PokeAPI is currently unavailable", ex);
    } catch (HttpClientException ex) {
      log.warn("PokeAPI lookup of '{}' failed: {}", idOrName, ex.getMessage());
      throw new CatalogUnavailableException("PokeAPI is currently unavailable", ex);
    }
  }
}
