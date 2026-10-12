package org.ballastlane.pokeapp.infrastructure.persistence;

import java.util.Optional;
import org.ballastlane.pokeapp.application.port.out.UserRepositoryPort;
import org.ballastlane.pokeapp.domain.exception.UsernameAlreadyTakenException;
import org.ballastlane.pokeapp.domain.model.User;
import org.ballastlane.pokeapp.infrastructure.persistence.entity.UserJpaEntity;
import org.ballastlane.pokeapp.infrastructure.persistence.repository.UserJpaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UserPersistenceAdapter implements UserRepositoryPort {

  private final UserJpaRepository repository;

  public UserPersistenceAdapter(UserJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<User> findByUsername(String username) {
    return repository.findByUsername(username).map(UserPersistenceAdapter::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsByUsername(String username) {
    return repository.existsByUsername(username);
  }

  @Override
  @Transactional
  public User save(User user) {
    try {
      var saved =
          repository.saveAndFlush(
              new UserJpaEntity(user.username(), user.passwordHash(), user.role()));
      return toDomain(saved);
    } catch (DataIntegrityViolationException ex) {
      throw new UsernameAlreadyTakenException(user.username());
    }
  }

  private static User toDomain(UserJpaEntity entity) {
    return new User(
        entity.getId(),
        entity.getUsername(),
        entity.getPasswordHash(),
        entity.getRole(),
        entity.getCreatedAt());
  }
}
