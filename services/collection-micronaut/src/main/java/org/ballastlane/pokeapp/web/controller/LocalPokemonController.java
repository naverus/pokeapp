package org.ballastlane.pokeapp.web.controller;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.validation.Validated;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.ballastlane.pokeapp.application.port.in.ManageLocalPokemonUseCase;
import org.ballastlane.pokeapp.application.port.in.ManageLocalPokemonUseCase.CreateLocalPokemonCommand;
import org.ballastlane.pokeapp.application.port.in.ManageLocalPokemonUseCase.UpdateLocalPokemonCommand;
import org.ballastlane.pokeapp.application.port.in.SyncPokemonUseCase;
import org.ballastlane.pokeapp.domain.exception.DomainValidationException;
import org.ballastlane.pokeapp.domain.model.LocalPokemon;
import org.ballastlane.pokeapp.domain.model.PageQuery;
import org.ballastlane.pokeapp.web.dto.PokemonDtos.CreateLocalPokemonRequest;
import org.ballastlane.pokeapp.web.dto.PokemonDtos.LocalPokemonResponse;
import org.ballastlane.pokeapp.web.dto.PokemonDtos.PageResponse;
import org.ballastlane.pokeapp.web.dto.PokemonDtos.StoredPokeApiIdsResponse;
import org.ballastlane.pokeapp.web.dto.PokemonDtos.UpdateLocalPokemonRequest;
import org.ballastlane.pokeapp.web.mapper.PokemonWebMapper;

@Controller("/api/v1/local-pokemon")
@Secured(SecurityRule.IS_AUTHENTICATED)
@ExecuteOn(TaskExecutors.BLOCKING)
@Validated
public class LocalPokemonController {

  private final ManageLocalPokemonUseCase manageLocalPokemon;
  private final SyncPokemonUseCase syncPokemon;

  public LocalPokemonController(
      ManageLocalPokemonUseCase manageLocalPokemon, SyncPokemonUseCase syncPokemon) {
    this.manageLocalPokemon = manageLocalPokemon;
    this.syncPokemon = syncPokemon;
  }

  @Get
  public PageResponse<LocalPokemonResponse> list(
      @QueryValue(defaultValue = "0") int page,
      @QueryValue(defaultValue = "20") int size,
      @QueryValue @Nullable String name) {
    var result = manageLocalPokemon.list(new PageQuery(page, size), name);
    return PokemonWebMapper.toPageResponse(result, PokemonWebMapper::toResponse);
  }

  /** Accepts both {@code pokeApiIds=1,2,3} and repeated {@code pokeApiIds=1&pokeApiIds=2}. */
  @Get("/stored-ids")
  public StoredPokeApiIdsResponse storedIds(HttpRequest<?> request) {
    var ids = parseIds(request.getParameters().getAll("pokeApiIds"));
    var stored = manageLocalPokemon.findStoredPokeApiIds(ids);
    return new StoredPokeApiIdsResponse(stored.stream().sorted().toList());
  }

  @Get("/{id}")
  public LocalPokemonResponse get(@PathVariable long id) {
    return PokemonWebMapper.toResponse(manageLocalPokemon.get(id));
  }

  @Post("/sync/{idOrName}")
  public HttpResponse<LocalPokemonResponse> sync(
      HttpRequest<?> request, @PathVariable String idOrName) {
    return created(request, syncPokemon.sync(idOrName));
  }

  @Post
  @Secured("ADMIN")
  public HttpResponse<LocalPokemonResponse> create(
      HttpRequest<?> request, @Body @Valid CreateLocalPokemonRequest body) {
    var command =
        new CreateLocalPokemonCommand(body.pokeApiId(), PokemonWebMapper.toAttributes(body));
    return created(request, manageLocalPokemon.create(command));
  }

  @Put("/{id}")
  public LocalPokemonResponse update(
      @PathVariable long id, @Body @Valid UpdateLocalPokemonRequest body) {
    var command =
        new UpdateLocalPokemonCommand(id, body.version(), PokemonWebMapper.toAttributes(body));
    return PokemonWebMapper.toResponse(manageLocalPokemon.update(command));
  }

  @Delete("/{id}")
  @Secured("ADMIN")
  public HttpResponse<Void> delete(@PathVariable long id) {
    manageLocalPokemon.delete(id);
    return HttpResponse.noContent();
  }

  private static HttpResponse<LocalPokemonResponse> created(
      HttpRequest<?> request, LocalPokemon pokemon) {
    var location = LocationUris.of(request, "/api/v1/local-pokemon/" + pokemon.id());
    return HttpResponse.created(PokemonWebMapper.toResponse(pokemon), location)
        .contentType(MediaType.APPLICATION_JSON_TYPE);
  }

  private static List<Integer> parseIds(List<String> rawValues) {
    var ids = new ArrayList<Integer>();
    for (var raw : rawValues) {
      for (var token : raw.split(",")) {
        var trimmed = token.strip();
        if (trimmed.isEmpty()) {
          continue;
        }
        try {
          ids.add(Integer.valueOf(trimmed));
        } catch (NumberFormatException ex) {
          throw new DomainValidationException(
              Map.of("pokeApiIds", "'%s' is not a valid id".formatted(trimmed)));
        }
      }
    }
    return ids;
  }
}
