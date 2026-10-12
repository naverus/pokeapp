package org.ballastlane.pokeapp.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.ballastlane.pokeapp.application.port.in.AuthenticateUserUseCase;
import org.ballastlane.pokeapp.application.port.in.AuthenticateUserUseCase.LoginCommand;
import org.ballastlane.pokeapp.application.port.in.RegisterUserUseCase;
import org.ballastlane.pokeapp.application.port.in.RegisterUserUseCase.RegisterUserCommand;
import org.ballastlane.pokeapp.web.dto.AuthDtos.LoginRequest;
import org.ballastlane.pokeapp.web.dto.AuthDtos.RegisterRequest;
import org.ballastlane.pokeapp.web.dto.AuthDtos.TokenResponse;
import org.ballastlane.pokeapp.web.dto.AuthDtos.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User registration and login (public)")
public class AuthController {

  private final RegisterUserUseCase registerUser;
  private final AuthenticateUserUseCase authenticateUser;

  public AuthController(
      RegisterUserUseCase registerUser, AuthenticateUserUseCase authenticateUser) {
    this.registerUser = registerUser;
    this.authenticateUser = authenticateUser;
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Register a new user with the USER role")
  public UserResponse register(@Valid @RequestBody RegisterRequest request) {
    var user =
        registerUser.register(new RegisterUserCommand(request.username(), request.password()));
    return new UserResponse(user.id(), user.username(), user.role());
  }

  @PostMapping("/login")
  @Operation(summary = "Exchange credentials for a JWT access token")
  public TokenResponse login(@Valid @RequestBody LoginRequest request) {
    var result =
        authenticateUser.authenticate(new LoginCommand(request.username(), request.password()));
    return new TokenResponse(
        result.accessToken(), "Bearer", result.expiresAt(), result.username(), result.role());
  }
}
