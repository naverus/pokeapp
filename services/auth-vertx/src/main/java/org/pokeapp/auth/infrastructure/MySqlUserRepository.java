package org.pokeapp.auth.infrastructure;

import io.vertx.core.Future;
import io.vertx.mysqlclient.MySQLException;
import io.vertx.mysqlclient.MySQLClient;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import java.util.Optional;
import org.pokeapp.auth.application.UserRepository;
import org.pokeapp.auth.domain.Role;
import org.pokeapp.auth.domain.User;
import org.pokeapp.auth.domain.UsernameAlreadyTakenException;

/** Reads and writes the {@code users} table owned by the collection service's Flyway migrations. */
public class MySqlUserRepository implements UserRepository {

  private static final int ER_DUP_ENTRY = 1062;

  private final Pool pool;

  public MySqlUserRepository(Pool pool) {
    this.pool = pool;
  }

  @Override
  public Future<Boolean> existsByUsername(String username) {
    return pool.preparedQuery("SELECT 1 FROM users WHERE username = ?")
        .execute(Tuple.of(username))
        .map(rows -> rows.size() > 0);
  }

  @Override
  public Future<Optional<User>> findByUsername(String username) {
    return pool.preparedQuery(
            "SELECT id, username, password_hash, role FROM users WHERE username = ?")
        .execute(Tuple.of(username))
        .map(
            rows -> {
              var iterator = rows.iterator();
              return iterator.hasNext() ? Optional.of(toUser(iterator.next())) : Optional.empty();
            });
  }

  @Override
  public Future<User> save(User user) {
    return pool.preparedQuery("INSERT INTO users (username, password_hash, role) VALUES (?, ?, ?)")
        .execute(Tuple.of(user.username(), user.passwordHash(), user.role().name()))
        .map(
            result ->
                new User(
                    result.property(MySQLClient.LAST_INSERTED_ID),
                    user.username(),
                    user.passwordHash(),
                    user.role()))
        .recover(
            failure ->
                failure instanceof MySQLException e && e.getErrorCode() == ER_DUP_ENTRY
                    ? Future.failedFuture(new UsernameAlreadyTakenException(user.username()))
                    : Future.failedFuture(failure));
  }

  private static User toUser(Row row) {
    return new User(
        row.getLong("id"),
        row.getString("username"),
        row.getString("password_hash"),
        Role.valueOf(row.getString("role")));
  }
}
