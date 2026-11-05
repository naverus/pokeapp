package org.ballastlane.pokeapp.catalog.infrastructure.pokeapi;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.ballastlane.pokeapp.catalog.application.port.out.PokemonCatalogPort;
import org.ballastlane.pokeapp.catalog.domain.exception.CatalogUnavailableException;
import org.ballastlane.pokeapp.catalog.domain.model.PageQuery;
import org.ballastlane.pokeapp.catalog.domain.model.PageResult;
import org.ballastlane.pokeapp.catalog.domain.model.PokemonDetail;
import org.ballastlane.pokeapp.catalog.domain.model.PokemonSummary;
import org.ballastlane.pokeapp.catalog.infrastructure.pokeapi.dto.PokeApiDtos;

/**
 * Implements the catalog port on top of PokeAPI. A page needs one list call plus a pokemon and a
 * species call per entry, so entries are resolved concurrently (bounded by {@code maxConcurrency}
 * in-flight entries to stay polite with the public API) and returned in PokeAPI order.
 */
@ApplicationScoped
public class PokeApiCatalogAdapter implements PokemonCatalogPort {

  private record Indexed(int index, PokemonSummary summary) {}

  private final PokeApiClient client;
  private final int maxConcurrency;

  @Inject
  public PokeApiCatalogAdapter(PokeApiClient client, PokeApiProperties properties) {
    this.client = client;
    this.maxConcurrency = Math.max(1, properties.maxConcurrency());
  }

  @Override
  public Uni<PageResult<PokemonSummary>> findPage(PageQuery query) {
    return client
        .fetchPokemonList(query.offset(), query.size())
        .flatMap(
            list -> {
              var names = list.results().stream().map(PokeApiDtos.NamedResource::name).toList();
              return resolveConcurrently(names)
                  .map(
                      summaries ->
                          new PageResult<>(summaries, query.page(), query.size(), list.count()));
            });
  }

  @Override
  public Uni<Optional<PokemonDetail>> findDetail(String idOrName) {
    return client
        .fetchPokemon(idOrName)
        .flatMap(
            pokemon ->
                client
                    .fetchSpecies(PokeApiMapper.idFromUrl(pokemon.species().url()))
                    .flatMap(
                        species ->
                            chainOf(species)
                                .map(
                                    chain ->
                                        Optional.of(
                                            PokeApiMapper.toDetail(pokemon, species, chain)))))
        .onFailure(PokeApiNotFoundException.class)
        .recoverWithItem(Optional.empty());
  }

  private Uni<PokeApiDtos.EvolutionChain> chainOf(PokeApiDtos.Species species) {
    if (species.evolutionChain() == null) {
      return Uni.createFrom().nullItem();
    }
    return client.fetchEvolutionChain(PokeApiMapper.idFromUrl(species.evolutionChain().url()));
  }

  private Uni<PokemonSummary> summaryOf(String name) {
    return client
        .fetchPokemon(name)
        .flatMap(
            pokemon ->
                client
                    .fetchSpecies(PokeApiMapper.idFromUrl(pokemon.species().url()))
                    .map(species -> PokeApiMapper.toSummary(pokemon, species)))
        .onFailure(PokeApiNotFoundException.class)
        .transform(
            ex ->
                new CatalogUnavailableException(
                    "PokeAPI listed '%s' but could not return it".formatted(name), ex));
  }

  private Uni<List<PokemonSummary>> resolveConcurrently(List<String> names) {
    if (names.isEmpty()) {
      return Uni.createFrom().item(List.of());
    }
    return Multi.createFrom()
        .range(0, names.size())
        .onItem()
        .transformToUni(index -> summaryOf(names.get(index)).map(s -> new Indexed(index, s)))
        .merge(maxConcurrency)
        .collect()
        .asList()
        .map(
            resolved ->
                resolved.stream()
                    .sorted(Comparator.comparingInt(Indexed::index))
                    .map(Indexed::summary)
                    .toList());
  }
}
