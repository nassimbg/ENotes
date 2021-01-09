package com.enotes.note.application;

import com.enotes.note.repository.authentication.InMemoryUserRepository;
import com.enotes.note.repository.authentication.UserRepository;
import com.enotes.note.service.authentication.AuthenticationService;
import com.enotes.note.service.authentication.util.JwtTokenProvider;
import com.enotes.note.service.authentication.util.TokenProvider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

  @Bean
  public AuthenticationService getAuthenticationService(UserRepository userRepository, TokenProvider tokenProvider) {
    return new AuthenticationService(userRepository, tokenProvider);
  }

  @Bean
  public TokenProvider getTokenProvider(StoreConfigProperties storeConfigProperties) {
    return new JwtTokenProvider(storeConfigProperties);
  }

  @Bean
  public UserRepository getUserRepository() {
    return new InMemoryUserRepository();
  }
}
