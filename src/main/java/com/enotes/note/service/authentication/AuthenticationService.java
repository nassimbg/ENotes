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
}
