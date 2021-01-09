package com.enotes.note.service.authentication;

import com.enotes.note.repository.authentication.UserRepository;
import com.enotes.note.service.authentication.util.TokenProvider;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationServiceTest {

  @Test
  public void testSignUp() {
    final User user = User.builder("John").withPassword("122334").build();

    TokenProvider tokenProvider = Mockito.mock(TokenProvider.class);
    Mockito.when(tokenProvider.generateToken(Mockito.any(), Mockito.any()))
        .thenReturn(new TokenInfo.Builder(user.getUserName()).build());

    UserRepository clientPersister = Mockito.mock(UserRepository.class);
    Mockito.when(clientPersister.putIfAbsent(Mockito.anyString(), Mockito.any())).thenReturn(true);

    final AuthenticationService authenticationService = new AuthenticationService(clientPersister, tokenProvider);

    assertEquals(user.getUserName(), authenticationService.signUp(user).getAccessToken());
  }

  @Test
  public void testSignUpWithUserNameAlreadyExist() {
    final User user = User.builder("John").withPassword("122334").build();

    TokenProvider tokenProvider = Mockito.mock(TokenProvider.class);
    UserRepository clientPersister = Mockito.mock(UserRepository.class);

    Mockito
        .doReturn(true)
        .doReturn(false)
        .when(clientPersister).putIfAbsent(Mockito.anyString(), Mockito.any());
    Mockito.when(tokenProvider.generateToken(Mockito.any(), Mockito.any()))
        .thenReturn(new TokenInfo.Builder(user.getUserName()).build());

    final AuthenticationService authenticationService = new AuthenticationService(clientPersister, tokenProvider);

    final String token = authenticationService.signUp(user).getAccessToken();
    assertEquals(user.getUserName(), token);

    Exception exception = assertThrows(AlreadyExistsException.class, () -> {
      authenticationService.signUp(user);
    });

    String expectedMessage = "Account with user name " + user.getUserName() + " already exist";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }
}