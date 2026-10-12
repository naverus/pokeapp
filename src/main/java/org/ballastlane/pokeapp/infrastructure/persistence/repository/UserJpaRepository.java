package org.ballastlane.pokeapp.infrastructure.persistence.repository;

import java.util.Optional;
import org.ballastlane.pokeapp.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {

  Optional<UserJpaEntity> findByUsername(String username);

  boolean existsByUsername(String username);
}
