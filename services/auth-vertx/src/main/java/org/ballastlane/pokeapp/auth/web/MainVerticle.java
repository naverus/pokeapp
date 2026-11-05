package org.ballastlane.pokeapp.auth.web;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.mysqlclient.MySQLBuilder;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import java.time.Clock;
import java.util.HashSet;
import java.util.Set;
import org.ballastlane.pokeapp.auth.application.AuthService;
import org.ballastlane.pokeapp.auth.infrastructure.AppConfig;
import org.ballastlane.pokeapp.auth.infrastructure.BCryptPasswordHasher;
import org.ballastlane.pokeapp.auth.infrastructure.JwtTokenService;
import org.ballastlane.pokeapp.auth.infrastructure.MySqlUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainVerticle extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(MainVerticle.class);
  private static final int MAX_BODY_BYTES = 64 * 1024;

  private final AppConfig config;
  private Pool pool;

  public MainVerticle(AppConfig config) {
    this.config = config;
  }

  @Override
  public void start(Promise<Void> startPromise) {
    var tokens =
        new JwtTokenService(
            config.jwtSecret(), config.jwtIssuer(), config.jwtTtl(), Clock.systemUTC());

    var db = config.db();
    var connectOptions =
        new MySQLConnectOptions()
            .setHost(db.host())
            .setPort(db.port())
            .setDatabase(db.database())
            .setUser(db.username())
            .setPassword(db.password())
            .setCharset("utf8mb4")
            .setReconnectAttempts(2)
            .setReconnectInterval(500);
    pool =
        MySQLBuilder.pool()
            .with(new PoolOptions().setMaxSize(10))
            .connectingTo(connectOptions)
            .using(vertx)
            .build();

    var mapper =
        new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    var problems = new ProblemResponses(mapper);
    var service =
        new AuthService(new MySqlUserRepository(pool), new BCryptPasswordHasher(vertx), tokens);
    var handlers = new AuthHandlers(service, mapper, problems);

    var router = Router.router(vertx);
    router
        .route("/api/*")
        .handler(
            CorsHandler.create()
                .addOrigins(config.corsAllowedOrigins())
                .allowedMethods(
                    Set.of(
                        HttpMethod.GET,
                        HttpMethod.POST,
                        HttpMethod.PUT,
                        HttpMethod.DELETE,
                        HttpMethod.OPTIONS))
                .allowedHeaders(new HashSet<>(Set.of("Authorization", "Content-Type")))
                .exposedHeaders(Set.of("Location")));
    router.get("/actuator/health").handler(this::health);
    router.post("/api/v1/auth/*").handler(BodyHandler.create().setBodyLimit(MAX_BODY_BYTES));
    router.post("/api/v1/auth/register").handler(handlers::register);
    router.post("/api/v1/auth/login").handler(handlers::login);

    router.errorHandler(404, ctx -> problems.status(ctx, 404, "Not Found", "No such resource"));
    router.errorHandler(
        405, ctx -> problems.status(ctx, 405, "Method Not Allowed", "Method not supported"));
    router.errorHandler(
        413, ctx -> problems.status(ctx, 413, "Payload Too Large", "Request body too large"));
    router.errorHandler(
        500,
        ctx -> {
          var failure = ctx.failure();
          if (failure != null) {
            problems.fail(ctx, failure);
          } else {
            problems.status(ctx, 500, "Internal error", "An unexpected error occurred");
          }
        });

    vertx
        .createHttpServer()
        .requestHandler(router)
        .listen(config.httpPort())
        .onSuccess(
            server -> {
              log.info("auth-vertx listening on port {}", server.actualPort());
              startPromise.complete();
            })
        .onFailure(startPromise::fail);
  }

  @Override
  public void stop() {
    if (pool != null) {
      pool.close();
    }
  }

  private void health(RoutingContext ctx) {
    ctx.response().putHeader("Content-Type", "application/json").end("{\"status\":\"UP\"}");
  }
}
