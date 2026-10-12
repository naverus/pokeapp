package org.ballastlane.pokeapp.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.ballastlane.pokeapp.application.port.in.ManageLocalPokemonUseCase;
import org.ballastlane.pokeapp.application.port.in.ManageLocalPokemonUseCase.CreateLocalPokemonCommand;
import org.ballastlane.pokeapp.application.port.in.ManageLocalPokemonUseCase.UpdateLocalPokemonCommand;
import org.ballastlane.pokeapp.application.port.in.SyncPokemonUseCase;
import org.ballastlane.pokeapp.domain.model.LocalPokemon;
import org.ballastlane.pokeapp.domain.model.PageQuery;
import org.ballastlane.pokeapp.web.dto.PokemonDtos.CreateLocalPokemonRequest;
import org.ballastlane.pokeapp.web.dto.PokemonDtos.LocalPokemonResponse;
import org.ballastlane.pokeapp.web.dto.PokemonDtos.PageResponse;
import org.ballastlane.pokeapp.web.dto.PokemonDtos.StoredPokeApiIdsResponse;
import org.ballastlane.pokeapp.web.dto.PokemonDtos.UpdateLocalPokemonRequest;
import org.ballastlane.pokeapp.web.mapper.PokemonWebMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/local-pokemon")
@Tag(name = "Local Pokemon", description = "Pokemon replicated in the local database (protected)")
@SecurityRequirement(name = "bearerAuth")
public class LocalPokemonController {

  private final ManageLocalPokemonUseCase manageLocalPokemon;
  private final SyncPokemonUseCase syncPokemon;

  public LocalPokemonController(
      ManageLocalPokemonUseCase manageLocalPokemon, SyncPokemonUseCase syncPokemon) {
    this.manageLocalPokemon = manageLocalPokemon;
    this.syncPokemon = syncPokemon;
  }

  @GetMapping
  @Operation(summary = "List local Pokemon, optionally filtered by (local) name")
  public PageResponse<LocalPokemonResponse> list(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) String name) {
    var result = manageLocalPokemon.list(new PageQuery(page, size), name);
    return PokemonWebMapper.toPageResponse(result, PokemonWebMapper::toResponse);
  }

  @GetMapping("/stored-ids")
  @Operation(summary = "Which of the given PokeAPI ids (1-100) are stored locally")
  public StoredPokeApiIdsResponse storedIds(@RequestParam List<Integer> pokeApiIds) {
    var stored = manageLocalPokemon.findStoredPokeApiIds(pokeApiIds);
    return new StoredPokeApiIdsResponse(stored.stream().sorted().toList());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get one local Pokemon")
  public LocalPokemonResponse get(@PathVariable long id) {
    return PokemonWebMapper.toResponse(manageLocalPokemon.get(id));
  }

  @PostMapping("/sync/{idOrName}")
  @Operation(summary = "US03 - Replicate a PokeAPI Pokemon into the local database")
  public ResponseEntity<LocalPokemonResponse> sync(@PathVariable String idOrName) {
    return created(syncPokemon.sync(idOrName));
  }

  @PostMapping
  @Operation(summary = "Create a custom local Pokemon (ADMIN)")
  public ResponseEntity<LocalPokemonResponse> create(
      @Valid @RequestBody CreateLocalPokemonRequest request) {
    var command =
        new CreateLocalPokemonCommand(request.pokeApiId(), PokemonWebMapper.toAttributes(request));
    return created(manageLocalPokemon.create(command));
  }

  @PutMapping("/{id}")
  @Operation(summary = "US04 - Update a local Pokemon (optimistic locking through 'version')")
  public LocalPokemonResponse update(
      @PathVariable long id, @Valid @RequestBody UpdateLocalPokemonRequest request) {
    var command =
        new UpdateLocalPokemonCommand(
            id, request.version(), PokemonWebMapper.toAttributes(request));
    return PokemonWebMapper.toResponse(manageLocalPokemon.update(command));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a local Pokemon (ADMIN)")
  public ResponseEntity<Void> delete(@PathVariable long id) {
    manageLocalPokemon.delete(id);
    return ResponseEntity.noContent().build();
  }

  private static ResponseEntity<LocalPokemonResponse> created(LocalPokemon pokemon) {
    var location =
        ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/api/v1/local-pokemon/{id}")
            .buildAndExpand(pokemon.id())
            .toUri();
    return ResponseEntity.created(location).body(PokemonWebMapper.toResponse(pokemon));
  }
}
