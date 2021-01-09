package com.enotes.note.service.authentication.util;

import com.enotes.note.repository.authentication.UserDetails;
import com.enotes.note.service.authentication.TokenInfo;

public interface TokenProvider {

  /**
   * Generates token per {@code user}
   *
   * @param user the user for which the token will be generated
   * @param type the type of token
   * @return the generated token
   */
  TokenInfo generateToken(UserDetails user, TokenType type);

  /**
   * validated the token
   *
   * @param authToken the token to be validated
   * @param type the type of token
   * @return true if the token is valid otherwise false
   */
  boolean validateToken(String authToken, TokenType type);

  /**
   * extracts the token information from the String token
   * @param authToken token which is used to extract the token info from
   * @param type the type of token
   * @return extracted token info
   */
  TokenInfo extractTokenInfo(String authToken, TokenType type);

  enum TokenType {
    ACCESS, REFRESH
  }
}
