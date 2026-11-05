package org.ballastlane.pokeapp.infrastructure.persistence.repository;

import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import org.ballastlane.pokeapp.infrastructure.persistence.entity.PokemonJpaEntity;

@Repository
public interface PokemonJpaRepository extends JpaRepository<PokemonJpaEntity, Long> {

  boolean existsByPokeApiId(int pokeApiId);

  Optional<PokemonJpaEntity> findByPokeApiId(int pokeApiId);

  @Query("select p.pokeApiId from PokemonJpaEntity p where p.pokeApiId in (:ids)")
  Set<Integer> findExistingPokeApiIds(Collection<Integer> ids);

  @Query(
      value =
          """
          select pokemonJpaEntity_ from PokemonJpaEntity pokemonJpaEntity_
          where lower(pokemonJpaEntity_.name) like lower(concat('%', :filter, '%'))
             or lower(pokemonJpaEntity_.localName) like lower(concat('%', :filter, '%'))
          """,
      countQuery =
          """
          select count(p) from PokemonJpaEntity p
          where lower(p.name) like lower(concat('%', :filter, '%'))
             or lower(p.localName) like lower(concat('%', :filter, '%'))
          """)
  Page<PokemonJpaEntity> searchByName(String filter, Pageable pageable);
}
