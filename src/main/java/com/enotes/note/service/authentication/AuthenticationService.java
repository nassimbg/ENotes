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

  private UserDetails authenticate(User user) {
    return userRepository.findById(user.getUserName())
        .filter(hashedUser -> Password.checkPassword(user.getPassword(), hashedUser.getPassword()))
    .orElseThrow(() -> new AuthenticationException("The username or password is incorrect"));
  }
}
