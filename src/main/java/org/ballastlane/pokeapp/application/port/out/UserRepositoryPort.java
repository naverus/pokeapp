package org.ballastlane.pokeapp.application.port.out;

import java.util.Optional;
import org.ballastlane.pokeapp.domain.model.User;

public interface UserRepositoryPort {

  Optional<User> findByUsername(String username);

  boolean existsByUsername(String username);

  User save(User user);
}
