package org.ballastlane.pokeapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class BallastlanePokeappApplication {

  public static void main(String[] args) {
    SpringApplication.run(BallastlanePokeappApplication.class, args);
  }
}
