package org.ballastlane.pokeapp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.ballastlane.pokeapp.infrastructure.security.JwtAuthenticationFilter;
import org.ballastlane.pokeapp.infrastructure.security.JwtTokenService;
import org.ballastlane.pokeapp.infrastructure.security.ProblemDetailSecurityHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private static final String LOCAL_POKEMON = "/api/v1/local-pokemon";

  @Bean
  SecurityFilterChain apiSecurityFilterChain(
      HttpSecurity http, JwtTokenService tokenService, ObjectMapper objectMapper) throws Exception {
    var problemHandler = new ProblemDetailSecurityHandler(objectMapper);
    return http.csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> {})
        .httpBasic(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .logout(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(
            handling ->
                handling
                    .authenticationEntryPoint(problemHandler)
                    .accessDeniedHandler(problemHandler))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/auth/register", "/api/v1/auth/login")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/pokemon", "/api/v1/pokemon/**")
                    .permitAll()
                    .requestMatchers(
                        "/error",
                        "/actuator/health/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html")
                    .permitAll()
                    .requestMatchers("/actuator/**", "/api/v1/admin/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, LOCAL_POKEMON)
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, LOCAL_POKEMON + "/**")
                    .hasRole("ADMIN")
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(
            new JwtAuthenticationFilter(tokenService), UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource(
      @Value("${pokeapp.cors.allowed-origins:http://localhost:5173}") String[] allowedOrigins) {
    var configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of(allowedOrigins));
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    configuration.setExposedHeaders(List.of("Location"));
    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", configuration);
    return source;
  }
}
