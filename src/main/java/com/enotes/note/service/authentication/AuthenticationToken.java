package com.enotes.note.service.authentication;

import java.util.Objects;

public class AuthenticationToken {

  private final String accessToken;
  private final String refreshToken;

  private AuthenticationToken() {
    accessToken = null;
    refreshToken = null;
  }

  public AuthenticationToken(final String accessToken, final String refreshToken) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final AuthenticationToken that = (AuthenticationToken) o;
    return Objects.equals(accessToken, that.accessToken) && Objects.equals(refreshToken,
        that.refreshToken);
  }

  @Override
  public int hashCode() {
    return Objects.hash(accessToken, refreshToken);
  }

  @Override public String toString() {
    return "AuthenticationResponse{" +
        "accessToken='" + accessToken + '\'' +
        ", refreshToken='" + refreshToken + '\'' +
        '}';
  }
}
