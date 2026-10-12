package org.ballastlane.pokeapp.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import org.ballastlane.pokeapp.application.port.in.AuthenticateUserUseCase;
import org.ballastlane.pokeapp.application.port.in.AuthenticateUserUseCase.AuthResult;
import org.ballastlane.pokeapp.application.port.in.AuthenticateUserUseCase.LoginCommand;
import org.ballastlane.pokeapp.application.port.in.RegisterUserUseCase;
import org.ballastlane.pokeapp.config.SecurityConfig;
import org.ballastlane.pokeapp.domain.exception.InvalidCredentialsException;
import org.ballastlane.pokeapp.domain.exception.UsernameAlreadyTakenException;
import org.ballastlane.pokeapp.domain.model.Role;
import org.ballastlane.pokeapp.domain.model.User;
import org.ballastlane.pokeapp.infrastructure.security.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private RegisterUserUseCase registerUser;
  @MockitoBean private AuthenticateUserUseCase authenticateUser;
  @MockitoBean private JwtTokenService tokenService;

  @Test
  void shouldRegisterAndReturn201_withoutExposingPassword() throws Exception {
    given(registerUser.register(any()))
        .willReturn(new User(3L, "misty", "hash", Role.USER, Instant.EPOCH));

    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"misty\", \"password\": \"starmie123\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.username").value("misty"))
        .andExpect(jsonPath("$.role").value("USER"))
        .andExpect(jsonPath("$.password").doesNotExist())
        .andExpect(jsonPath("$.passwordHash").doesNotExist());
  }

  @Test
  void shouldReturn400_whenRegistrationPayloadInvalid() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"a b\", \"password\": \"123\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.username").exists())
        .andExpect(jsonPath("$.errors.password").exists());
  }

  @Test
  void shouldReturn409_whenUsernameTaken() throws Exception {
    given(registerUser.register(any())).willThrow(new UsernameAlreadyTakenException("ash"));

    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"ash\", \"password\": \"pikachu123\"}"))
        .andExpect(status().isConflict());
  }

  @Test
  void shouldLoginAndReturnBearerToken() throws Exception {
    given(authenticateUser.authenticate(new LoginCommand("ash", "pikachu123")))
        .willReturn(new AuthResult("jwt", Instant.parse("2026-10-12T14:00:00Z"), "ash", Role.USER));

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"ash\", \"password\": \"pikachu123\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("jwt"))
        .andExpect(jsonPath("$.tokenType").value("Bearer"))
        .andExpect(jsonPath("$.role").value("USER"));
  }

  @Test
  void shouldReturn401_whenCredentialsInvalid() throws Exception {
    given(authenticateUser.authenticate(any())).willThrow(new InvalidCredentialsException());

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"ash\", \"password\": \"wrong\"}"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.detail").value("Invalid username or password"));
  }
}
