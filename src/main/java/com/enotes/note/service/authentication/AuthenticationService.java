package com.enotes.note.service.authentication;

import com.enotes.note.repository.authentication.UserDetails;
import com.enotes.note.repository.authentication.UserRepository;
import com.enotes.note.service.authentication.util.Password;
import com.enotes.note.service.authentication.util.TokenProvider;

public class AuthenticationService {

  private final UserRepository userRepository;
  private final TokenProvider tokenProvider;

  public AuthenticationService(final UserRepository userRepository,
      final TokenProvider tokenProvider) {
    this.userRepository = userRepository;
    this.tokenProvider = tokenProvider;
  }

  /**
   * SignUp the given user and returns a valid token
   * @param user user to signUp
   * @return valid token unique for the user
   * @throws AlreadyExistsException if the same user name for the user exists
   */
  public AuthenticationToken signUp(User user) {
    UserDetails storedUser = hashUserInfo(user);

    final String accessToken = this.tokenProvider.generateToken(storedUser, TokenProvider.TokenType.ACCESS).getToken();
    final TokenInfo refreshToken = this.tokenProvider.generateToken(storedUser, TokenProvider.TokenType.REFRESH);

    storedUser = storedUser.toBuilder()
        .isTokenValid(true)
        .withRefreshTokenId(refreshToken.getId())
        .build();
    final boolean added = userRepository.putIfAbsent(storedUser.getUserName(), storedUser);

    if (!added) {
      throw new AlreadyExistsException("Account with user name " + user.getUserName() + " already exist");
    }
    return new AuthenticationToken(accessToken, refreshToken.getToken());
  }

  static UserDetails hashUserInfo(User user) {
    return new UserDetails.Builder(user.getUserName(), Password.hashPassword(user.getPassword()))
        .build();
  }

  /**
   * Sign in the given user and returns a valid token
   * @param user user to signUp
   * @return valid token unique for the user
   * @throws AuthenticationException if the user name or password is incorrect
   */
  public AuthenticationToken signIn(User user) {
    UserDetails storedUser = authenticate(user);

    final TokenInfo accessToken = this.tokenProvider.generateToken(storedUser, TokenProvider.TokenType.ACCESS);
    final TokenInfo refreshToken = this.tokenProvider.generateToken(storedUser, TokenProvider.TokenType.REFRESH);

    storedUser = storedUser.toBuilder()
        .isTokenValid(true)
        .withRefreshTokenId(refreshToken.getId())
        .build();
    this.userRepository.put(storedUser.getUserName(), storedUser);

    return new AuthenticationToken(accessToken.getToken(), refreshToken.getToken());
  }

  /**
   * Given the {@link AuthenticationToken} it tries to generate a new access token if the refresh token is valid
   * @param authenticationToken the authentication token that contains that is used to generate the new token
   * @return return a {@link AuthenticationToken} with the new access token and same refresh token if the incoming
   * {@code authenticationToken} is valid
   * @throws AuthenticationException if the incoming {@code authenticationToken} is invalid
   */
  public AuthenticationToken refresh(final AuthenticationToken authenticationToken) {
    final String refreshToken = authenticationToken.getRefreshToken();

    final UserDetails storedUser = getUserDetailsAndValidate(refreshToken);

    final String accessToken = this.tokenProvider.generateToken(storedUser, TokenProvider.TokenType.ACCESS).getToken();

    return new AuthenticationToken(accessToken, refreshToken);
  }

  /**
   * Given the {@link AuthenticationToken} it tries to sign out the user if the refresh token is valid
   * @param authenticationToken the authentication token that contains that is used to generate the new token
   * @return return a {@link UserStatus} containing the user name and log out status set to true if the incoming
   * {@code authenticationToken} is valid
   * @throws AuthenticationException if the incoming {@code authenticationToken} is invalid
   */
  public UserStatus signOut(AuthenticationToken authenticationToken) {
    final String refreshToken = authenticationToken.getRefreshToken();
    UserDetails storedUser = getUserDetailsAndValidate(refreshToken);

    storedUser = storedUser.toBuilder().isTokenValid(false).build();
    userRepository.put(storedUser.getUserName(), storedUser);

    return new UserStatus(storedUser.getUserName(), true);
  }

  private UserDetails authenticate(User user) {
    return userRepository.findById(user.getUserName())
        .filter(hashedUser -> Password.checkPassword(user.getPassword(), hashedUser.getPassword()))
    .orElseThrow(() -> new AuthenticationException("The username or password is incorrect"));
  }

  private UserDetails getUserDetailsAndValidate(final String refreshToken) {
    final TokenInfo tokenInfo = tokenProvider.extractTokenInfo(refreshToken, TokenProvider.TokenType.REFRESH);
    final UserDetails storedUser = userRepository.findById(tokenInfo.getUserName())
        .orElseThrow(() -> new AuthenticationException("The object you requested does not exist"));

    if (!tokenInfo.isValid() || storedUser.isTokenInValidated()
        || !storedUser.getRefreshTokenId().equals(tokenInfo.getId())) {
      throw new AuthenticationException("The provided Refresh Token is either expired or has been revoked");
    }
    return storedUser;
  }
}
