package org.ballastlane.pokeapp.infrastructure.persistence.repository;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import org.ballastlane.pokeapp.infrastructure.persistence.entity.PokemonJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PokemonJpaRepository extends JpaRepository<PokemonJpaEntity, Long> {

  boolean existsByPokeApiId(int pokeApiId);

  Optional<PokemonJpaEntity> findByPokeApiId(int pokeApiId);

  @Query("select p.pokeApiId from PokemonJpaEntity p where p.pokeApiId in :ids")
  Set<Integer> findExistingPokeApiIds(@Param("ids") Collection<Integer> ids);

  @Query(
      """
      select p from PokemonJpaEntity p
      where lower(p.name) like lower(concat('%', :filter, '%'))
         or lower(p.localName) like lower(concat('%', :filter, '%'))
      """)
  Page<PokemonJpaEntity> searchByName(@Param("filter") String filter, Pageable pageable);
}
