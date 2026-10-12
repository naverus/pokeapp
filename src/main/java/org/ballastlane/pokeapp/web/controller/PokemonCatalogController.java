package org.ballastlane.pokeapp.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Duration;
import org.ballastlane.pokeapp.application.port.in.BrowsePokemonUseCase;
import org.ballastlane.pokeapp.application.port.in.GetPokemonDetailUseCase;
import org.ballastlane.pokeapp.domain.model.PageQuery;
import org.ballastlane.pokeapp.web.dto.PokemonDtos.PageResponse;
import org.ballastlane.pokeapp.web.dto.PokemonDtos.PokemonDetailResponse;
import org.ballastlane.pokeapp.web.dto.PokemonDtos.PokemonSummaryResponse;
import org.ballastlane.pokeapp.web.mapper.PokemonWebMapper;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pokemon")
@Tag(name = "Catalog", description = "Live PokeAPI data (public, cached)")
public class PokemonCatalogController {

  private static final CacheControl CLIENT_CACHE = CacheControl.maxAge(Duration.ofMinutes(5));

  private final BrowsePokemonUseCase browsePokemon;
  private final GetPokemonDetailUseCase getPokemonDetail;

  public PokemonCatalogController(
      BrowsePokemonUseCase browsePokemon, GetPokemonDetailUseCase getPokemonDetail) {
    this.browsePokemon = browsePokemon;
    this.getPokemonDetail = getPokemonDetail;
  }

  @GetMapping
  @Operation(summary = "US01 - Browse Pokemon with sprite, category, mass and abilities")
  public ResponseEntity<PageResponse<PokemonSummaryResponse>> browse(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    var result = browsePokemon.browse(new PageQuery(page, size));
    return ResponseEntity.ok()
        .cacheControl(CLIENT_CACHE)
        .body(PokemonWebMapper.toPageResponse(result, PokemonWebMapper::toResponse));
  }

  @GetMapping("/{idOrName}")
  @Operation(summary = "US02 - Image, stats, description and evolution chain of a Pokemon")
  public ResponseEntity<PokemonDetailResponse> detail(@PathVariable String idOrName) {
    return ResponseEntity.ok()
        .cacheControl(CLIENT_CACHE)
        .body(PokemonWebMapper.toResponse(getPokemonDetail.getDetail(idOrName)));
  }
}
