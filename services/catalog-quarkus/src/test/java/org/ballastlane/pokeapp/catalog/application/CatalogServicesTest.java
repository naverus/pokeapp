package org.ballastlane.pokeapp.catalog.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.smallrye.mutiny.Uni;
import java.util.List;
import java.util.Optional;
import org.ballastlane.pokeapp.catalog.application.port.out.PokemonCatalogPort;
import org.ballastlane.pokeapp.catalog.application.service.BrowsePokemonService;
import org.ballastlane.pokeapp.catalog.application.service.GetPokemonDetailService;
import org.ballastlane.pokeapp.catalog.domain.exception.PokemonNotFoundException;
import org.ballastlane.pokeapp.catalog.domain.model.PageQuery;
import org.ballastlane.pokeapp.catalog.domain.model.PageResult;
import org.ballastlane.pokeapp.catalog.domain.model.PokemonDetail;
import org.ballastlane.pokeapp.catalog.domain.model.PokemonSummary;
import org.junit.jupiter.api.Test;

class CatalogServicesTest {

  private static final PokemonSummary PIKACHU =
      new PokemonSummary(25, "pikachu", "s.png", "Mouse", 6.0, List.of("static"), List.of("electric"));
  private static final PokemonDetail PIKACHU_DETAIL =
      new PokemonDetail(
          25, "pikachu", "i.png", "Mouse", 0.4, 6.0, List.of("electric"), List.of("static"), List.of(), "d", List.of());

  private final PokemonCatalogPort port =
      new PokemonCatalogPort() {
        @Override
        public Uni<PageResult<PokemonSummary>> findPage(PageQuery query) {
          return Uni.createFrom().item(new PageResult<>(List.of(PIKACHU), query.page(), query.size(), 1));
        }

        @Override
        public Uni<Optional<PokemonDetail>> findDetail(String idOrName) {
          return Uni.createFrom()
              .item("pikachu".equals(idOrName) ? Optional.of(PIKACHU_DETAIL) : Optional.empty());
        }
      };

  @Test
  void browseDelegatesToCatalog() {
    var page = new BrowsePokemonService(port).browse(new PageQuery(0, 20)).await().indefinitely();

    assertThat(page.content()).containsExactly(PIKACHU);
    assertThat(page.totalElements()).isEqualTo(1);
  }

  @Test
  void detailNormalisesIdentifier() {
    var detail = new GetPokemonDetailService(port).getDetail(" Pikachu ").await().indefinitely();

    assertThat(detail).isEqualTo(PIKACHU_DETAIL);
  }

  @Test
  void detailFailsWithNotFoundWhenCatalogHasNothing() {
    var uni = new GetPokemonDetailService(port).getDetail("ghost");

    assertThatThrownBy(() -> uni.await().indefinitely()).isInstanceOf(PokemonNotFoundException.class);
  }
}
