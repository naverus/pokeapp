package org.pokeapp.auth;

import io.vertx.core.Vertx;
import org.pokeapp.auth.infrastructure.AppConfig;
import org.pokeapp.auth.web.MainVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Main {

  private static final Logger log = LoggerFactory.getLogger(Main.class);

  private Main() {}

  public static void main(String[] args) {
    var config = AppConfig.fromEnvironment();
    var vertx = Vertx.vertx();
    vertx
        .deployVerticle(new MainVerticle(config))
        .onFailure(
            e -> {
              log.error("Failed to start auth-vertx: {}", e.getMessage(), e);
              vertx.close().onComplete(done -> System.exit(1));
            });
    Runtime.getRuntime().addShutdownHook(new Thread(() -> vertx.close().toCompletionStage().toCompletableFuture().join()));
  }
}
