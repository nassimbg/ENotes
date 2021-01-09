package com.enotes.note.service.authentication;

import com.enotes.note.repository.authentication.UserDetails;
import com.enotes.note.repository.authentication.UserRepository;
import com.enotes.note.service.authentication.util.TokenProvider;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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

  @Test
  public void testSignInForExistingUser() {
    final User user = User.builder("John").withPassword("122334").build();
    TokenProvider tokenProvider = Mockito.mock(TokenProvider.class);
    UserRepository clientPersister = Mockito.mock(UserRepository.class);

    Mockito.when(tokenProvider.generateToken(Mockito.any(), Mockito.any()))
        .thenReturn(new TokenInfo.Builder(user.getUserName()).build());
    Mockito.when(clientPersister.putIfAbsent(Mockito.anyString(), Mockito.any())).thenReturn(true);

    final AuthenticationService authenticationService = new AuthenticationService(clientPersister, tokenProvider);

    final String token = authenticationService.signUp(user).getAccessToken();

    assertEquals(user.getUserName(), token);

    Mockito.when(clientPersister.findById(Mockito.eq(user.getUserName())))
        .thenReturn(Optional.of(AuthenticationService.hashUserInfo(user)));
    final String signInToken = authenticationService.signIn(user).getAccessToken();

    assertEquals(user.getUserName(), signInToken);
  }

  @Test
  public void testSignInForNotExistingUser() {
    final User user = User.builder("John").withPassword("122334").build();

    TokenProvider tokenProvider = Mockito.mock(TokenProvider.class);
    Mockito.when(tokenProvider.generateToken(Mockito.any(), Mockito.any()))
        .thenReturn(new TokenInfo.Builder(user.getUserName()).build());

    UserRepository clientPersister = Mockito.mock(UserRepository.class);
    Mockito.when(clientPersister.findById(Mockito.eq(user.getUserName()))).thenReturn(Optional.empty());

    final AuthenticationService authenticationService = new AuthenticationService(clientPersister, tokenProvider);


    Exception exception = assertThrows(AuthenticationException.class, () -> {
      authenticationService.signIn(user);
    });

    String expectedMessage = "The username or password is incorrect";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void testRefreshToken() {
    final String accessTokenValue = "accessTokenValue";
    final String refreshTokenValue = "refreshTokenValue";
    AuthenticationToken authenticationToken = new AuthenticationToken(accessTokenValue, refreshTokenValue);

    final String userName = "John";
    final String refreshTokenId = "1223";

    final UserDetails user = new UserDetails.Builder(userName, "122334")
        .isTokenValid(true)
        .withRefreshTokenId(refreshTokenId)
        .build();
    TokenProvider tokenProvider = Mockito.mock(TokenProvider.class);
    UserRepository clientPersister = Mockito.mock(UserRepository.class);

    final String newAccessToken = "newAccessToken";
    Mockito.when(tokenProvider.generateToken(Mockito.any(), Mockito.any())).thenReturn(new TokenInfo.Builder(newAccessToken).build());

    final TokenInfo tokenInfo = new TokenInfo.Builder(refreshTokenValue).isValid(true).userName(userName).id(refreshTokenId).build();
    Mockito.when(tokenProvider.extractTokenInfo(Mockito.any(), Mockito.any())).thenReturn(tokenInfo);
    Mockito.when(clientPersister.findById(Mockito.eq(userName))).thenReturn(Optional.of(user));

    final AuthenticationService authenticationService = new AuthenticationService(clientPersister, tokenProvider);

    final AuthenticationToken refreshedToken = authenticationService.refresh(authenticationToken);

    assertEquals(refreshTokenValue, refreshedToken.getRefreshToken());
    assertEquals(newAccessToken, refreshedToken.getAccessToken());
  }

  @Test
  public void testFailingToRefreshTokenDueToTimeExpiring() {
    final String accessTokenValue = "accessTokenValue";
    final String refreshTokenValue = "refreshTokenValue";
    AuthenticationToken authenticationToken = new AuthenticationToken(accessTokenValue, refreshTokenValue);

    final String userName = "John";
    TokenProvider tokenProvider = Mockito.mock(TokenProvider.class);
    UserRepository clientPersister = Mockito.mock(UserRepository.class);

    final TokenInfo tokenInfo = new TokenInfo.Builder(refreshTokenValue).isValid(false).userName(userName).id("1223").build();
    Mockito.when(tokenProvider.extractTokenInfo(Mockito.any(), Mockito.any())).thenReturn(tokenInfo);

    Mockito.when(clientPersister.findById(Mockito.eq(userName)))
        .thenReturn(Optional.of(Mockito.mock(UserDetails.class)));

    final AuthenticationService authenticationService = new AuthenticationService(clientPersister, tokenProvider);

    Exception exception = assertThrows(AuthenticationException.class, () -> {
      authenticationService.refresh(authenticationToken);
    });

    String expectedMessage = "The provided Refresh Token is either expired or has been revoked";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void testFailingToRefreshTokenDueUserDoesntExist() {
    final String accessTokenValue = "accessTokenValue";
    final String refreshTokenValue = "refreshTokenValue";
    AuthenticationToken authenticationToken = new AuthenticationToken(accessTokenValue, refreshTokenValue);

    final String userName = "John";
    TokenProvider tokenProvider = Mockito.mock(TokenProvider.class);
    UserRepository clientPersister = Mockito.mock(UserRepository.class);

    final String newAccessToken = "newAccessToken";
    Mockito.when(tokenProvider.generateToken(Mockito.any(), Mockito.any()))
        .thenReturn(new TokenInfo.Builder(newAccessToken).build());

    final TokenInfo tokenInfo = new TokenInfo.Builder(refreshTokenValue).isValid(true).userName(userName).id("1223").build();
    Mockito.when(tokenProvider.extractTokenInfo(Mockito.any(), Mockito.any())).thenReturn(tokenInfo);
    Mockito.when(clientPersister.findById(Mockito.eq(userName))).thenReturn(Optional.empty());

    final AuthenticationService authenticationService = new AuthenticationService(clientPersister, tokenProvider);

    Exception exception = assertThrows(AuthenticationException.class, () -> {
      authenticationService.refresh(authenticationToken);
    });

    String expectedMessage = "The object you requested does not exist";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void testFailingToRefreshTokenDueUserInvalidatedToken() {
    final String accessTokenValue = "accessTokenValue";
    final String refreshTokenValue = "refreshTokenValue";
    AuthenticationToken authenticationToken = new AuthenticationToken(accessTokenValue, refreshTokenValue);

    final String userName = "John";
    final String refreshTokenId = "1223";

    final UserDetails user = new UserDetails.Builder(userName, "122334")
        .isTokenValid(false)
        .withRefreshTokenId(refreshTokenId)
        .build();
    TokenProvider tokenProvider = Mockito.mock(TokenProvider.class);
    UserRepository clientPersister = Mockito.mock(UserRepository.class);

    final String newAccessToken = "newAccessToken";
    Mockito.when(tokenProvider.generateToken(Mockito.any(), Mockito.any())).thenReturn(new TokenInfo.Builder(newAccessToken).build());

    final TokenInfo tokenInfo = new TokenInfo.Builder(refreshTokenValue).isValid(true).userName(userName).id(refreshTokenId).build();
    Mockito.when(tokenProvider.extractTokenInfo(Mockito.any(), Mockito.any())).thenReturn(tokenInfo);
    Mockito.when(clientPersister.findById(Mockito.eq(userName))).thenReturn(Optional.of(user));

    final AuthenticationService authenticationService = new AuthenticationService(clientPersister, tokenProvider);

    Exception exception = assertThrows(AuthenticationException.class, () -> {
      authenticationService.refresh(authenticationToken);
    });

    String expectedMessage = "The provided Refresh Token is either expired or has been revoked";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void testFailingToRefreshTokenDueTokenIdIsDifferentFromAlreadyStored() {
    final String accessTokenValue = "accessTokenValue";
    final String refreshTokenValue = "refreshTokenValue";
    AuthenticationToken authenticationToken = new AuthenticationToken(accessTokenValue, refreshTokenValue);

    final String userName = "John";
    final String refreshTokenId = "1223";

    final String anotherRefreshToken = "zzzzz";
    final UserDetails user = new UserDetails.Builder(userName, "122334")
        .isTokenValid(true)
        .withRefreshTokenId(anotherRefreshToken)
        .build();
    TokenProvider tokenProvider = Mockito.mock(TokenProvider.class);
    UserRepository clientPersister = Mockito.mock(UserRepository.class);

    final String newAccessToken = "newAccessToken";
    Mockito.when(tokenProvider.generateToken(Mockito.any(), Mockito.any())).thenReturn(new TokenInfo.Builder(newAccessToken).build());

    final TokenInfo tokenInfo = new TokenInfo.Builder(refreshTokenValue).isValid(true).userName(userName).id(refreshTokenId).build();
    Mockito.when(tokenProvider.extractTokenInfo(Mockito.any(), Mockito.any())).thenReturn(tokenInfo);
    Mockito.when(clientPersister.findById(Mockito.eq(userName))).thenReturn(Optional.of(user));

    final AuthenticationService authenticationService = new AuthenticationService(clientPersister, tokenProvider);

    Exception exception = assertThrows(AuthenticationException.class, () -> {
      authenticationService.refresh(authenticationToken);
    });

    String expectedMessage = "The provided Refresh Token is either expired or has been revoked";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void testSignOut() {
    final String accessTokenValue = "accessTokenValue";
    final String refreshTokenValue = "refreshTokenValue";
    AuthenticationToken authenticationToken = new AuthenticationToken(accessTokenValue, refreshTokenValue);

    final String userName = "John";
    final String refreshTokenId = "1223";

    final UserDetails user = new UserDetails.Builder(userName, "122334")
        .isTokenValid(true)
        .withRefreshTokenId(refreshTokenId)
        .build();
    TokenProvider tokenProvider = Mockito.mock(TokenProvider.class);
    UserRepository clientPersister = Mockito.mock(UserRepository.class);

    final TokenInfo tokenInfo = new TokenInfo.Builder(refreshTokenValue).isValid(true).userName(userName).id(refreshTokenId).build();
    Mockito.when(tokenProvider.extractTokenInfo(Mockito.any(), Mockito.any())).thenReturn(tokenInfo);
    Mockito.when(clientPersister.findById(Mockito.eq(userName))).thenReturn(Optional.of(user));

    final AuthenticationService authenticationService = new AuthenticationService(clientPersister, tokenProvider);

    final UserStatus userStatus = authenticationService.signOut(authenticationToken);

    verify(clientPersister, times(1)).put(userName, user.toBuilder().isTokenValid(false).build());
    assertEquals(userName, userStatus.getUserName());
    assertTrue(userStatus.logOut());
  }
}
