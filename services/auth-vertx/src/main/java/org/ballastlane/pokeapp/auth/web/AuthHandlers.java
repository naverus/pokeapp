package org.ballastlane.pokeapp.auth.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.ext.web.RoutingContext;
import org.ballastlane.pokeapp.auth.application.AuthService;
import org.ballastlane.pokeapp.auth.web.AuthDtos.LoginRequest;
import org.ballastlane.pokeapp.auth.web.AuthDtos.RegisterRequest;
import org.ballastlane.pokeapp.auth.web.AuthDtos.TokenResponse;
import org.ballastlane.pokeapp.auth.web.AuthDtos.UserResponse;

public class AuthHandlers {

  private final AuthService auth;
  private final ObjectMapper mapper;
  private final ProblemResponses problems;

  public AuthHandlers(AuthService auth, ObjectMapper mapper, ProblemResponses problems) {
    this.auth = auth;
    this.mapper = mapper;
    this.problems = problems;
  }

  public void register(RoutingContext ctx) {
    final RegisterRequest request;
    try {
      request = read(ctx, RegisterRequest.class);
      request.validate();
    } catch (RuntimeException e) {
      problems.fail(ctx, e);
      return;
    }
    auth.register(request.username(), request.password())
        .onSuccess(user -> json(ctx, 201, new UserResponse(user.id(), user.username(), user.role())))
        .onFailure(e -> problems.fail(ctx, e));
  }

  public void login(RoutingContext ctx) {
    final LoginRequest request;
    try {
      request = read(ctx, LoginRequest.class);
      request.validate();
    } catch (RuntimeException e) {
      problems.fail(ctx, e);
      return;
    }
    auth.login(request.username(), request.password())
        .onSuccess(
            result ->
                json(
                    ctx,
                    200,
                    new TokenResponse(
                        result.accessToken(),
                        "Bearer",
                        result.expiresAt(),
                        result.username(),
                        result.role())))
        .onFailure(e -> problems.fail(ctx, e));
  }

  private <T> T read(RoutingContext ctx, Class<T> type) {
    try {
      var body = ctx.body().buffer();
      if (body == null || body.length() == 0) {
        throw new ProblemResponses.MalformedBodyException(null);
      }
      var value = mapper.readValue(body.getBytes(), type);
      if (value == null) {
        throw new ProblemResponses.MalformedBodyException(null);
      }
      return value;
    } catch (java.io.IOException e) {
      throw new ProblemResponses.MalformedBodyException(e);
    }
  }

  private void json(RoutingContext ctx, int status, Object body) {
    try {
      ctx.response()
          .setStatusCode(status)
          .putHeader("Content-Type", "application/json")
          .end(mapper.writeValueAsString(body));
    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
      problems.fail(ctx, e);
    }
  }
}
