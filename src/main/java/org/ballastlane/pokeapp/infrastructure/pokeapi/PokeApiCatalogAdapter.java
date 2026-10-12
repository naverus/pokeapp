package org.ballastlane.pokeapp.infrastructure.pokeapi;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import org.ballastlane.pokeapp.application.port.out.PokemonCatalogPort;
import org.ballastlane.pokeapp.domain.exception.CatalogUnavailableException;
import org.ballastlane.pokeapp.domain.model.PageQuery;
import org.ballastlane.pokeapp.domain.model.PageResult;
import org.ballastlane.pokeapp.domain.model.PokemonDetail;
import org.ballastlane.pokeapp.domain.model.PokemonSummary;
import org.ballastlane.pokeapp.infrastructure.pokeapi.dto.PokeApiDtos;
import org.springframework.stereotype.Component;

/**
 * Implements the catalog port on top of PokeAPI. A page needs one list call plus a pokemon and a
 * species call per entry, so entries are resolved concurrently on virtual threads (bounded by
 * {@code maxConcurrency} to stay polite with the public API).
 */
@Component
public class PokeApiCatalogAdapter implements PokemonCatalogPort {

  private final PokeApiClient client;
  private final int maxConcurrency;

  public PokeApiCatalogAdapter(PokeApiClient client, PokeApiProperties properties) {
    this.client = client;
    this.maxConcurrency = Math.max(1, properties.maxConcurrency());
  }

  @Override
  public PageResult<PokemonSummary> findPage(PageQuery query) {
    var list = client.fetchPokemonList(query.offset(), query.size());
    var names = list.results().stream().map(PokeApiDtos.NamedResource::name).toList();
    var summaries = resolveConcurrently(names);
    return new PageResult<>(summaries, query.page(), query.size(), list.count());
  }

  @Override
  public Optional<PokemonDetail> findDetail(String idOrName) {
    try {
      var pokemon = client.fetchPokemon(idOrName);
      var species = client.fetchSpecies(PokeApiMapper.idFromUrl(pokemon.species().url()));
      var chain =
          species.evolutionChain() == null
              ? null
              : client.fetchEvolutionChain(PokeApiMapper.idFromUrl(species.evolutionChain().url()));
      return Optional.of(PokeApiMapper.toDetail(pokemon, species, chain));
    } catch (PokeApiNotFoundException ex) {
      return Optional.empty();
    }
  }

  private PokemonSummary summaryOf(String name) {
    try {
      var pokemon = client.fetchPokemon(name);
      var species = client.fetchSpecies(PokeApiMapper.idFromUrl(pokemon.species().url()));
      return PokeApiMapper.toSummary(pokemon, species);
    } catch (PokeApiNotFoundException ex) {
      throw new CatalogUnavailableException(
          "PokeAPI listed '%s' but could not return it".formatted(name), ex);
    }
  }

  private List<PokemonSummary> resolveConcurrently(List<String> names) {
    var permits = new Semaphore(maxConcurrency);
    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      List<Future<PokemonSummary>> futures =
          names.stream()
              .map(name -> executor.submit(bounded(permits, () -> summaryOf(name))))
              .toList();
      return futures.stream().map(PokeApiCatalogAdapter::await).toList();
    }
  }

  private static <T> Callable<T> bounded(Semaphore permits, Callable<T> task) {
    return () -> {
      permits.acquire();
      try {
        return task.call();
      } finally {
        permits.release();
      }
    };
  }

  private static <T> T await(Future<T> future) {
    try {
      return future.get();
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new CatalogUnavailableException("Interrupted while calling PokeAPI", ex);
    } catch (ExecutionException ex) {
      if (ex.getCause() instanceof RuntimeException runtime) {
        throw runtime;
      }
      throw new CatalogUnavailableException("PokeAPI call failed", ex.getCause());
    }
  }
}
