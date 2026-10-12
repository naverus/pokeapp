package org.ballastlane.pokeapp.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class MySqlTestcontainersConfig {

  @Bean
  @ServiceConnection
  MySQLContainer<?> mysqlContainer() {
    return new MySQLContainer<>(DockerImageName.parse("mysql:8.4"))
        .withDatabaseName("pokeapp")
        .withUsername("pokeapp")
        .withPassword("pokeapp")
        .withReuse(false);
  }
}
