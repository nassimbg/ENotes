package com.enotes.note.repository.authentication;

import java.util.Objects;

public class UserDetails {

  private String userName;
  private String password;
  private String refreshTokenId;
  private boolean isTokenValid;

  private UserDetails(final Builder builder) {
    this.userName = builder.userName;
    this.password = builder.password;
    this.refreshTokenId = builder.refreshTokenId;
    this.isTokenValid = builder.isTokenValid;
  }

  public UserDetails() {
    //do nothing
  }

  public String getUserName() {
    return userName;
  }

  public String getPassword() {
    return password;
  }

  public String getRefreshTokenId() {
    return refreshTokenId;
  }

  public boolean isTokenInValidated() {
    return !isTokenValid;
  }

  public Builder toBuilder() {
    return new Builder(userName, password);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final UserDetails that = (UserDetails) o;
    return isTokenValid == that.isTokenValid
        && Objects.equals(userName, that.userName)
        && Objects.equals(password, that.password)
        && Objects.equals(refreshTokenId, that.refreshTokenId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userName, password, refreshTokenId, isTokenValid);
  }

  @Override
  public String toString() {
    return "UserDetails{" +
        "userName='" + userName + '\'' +
        ", password='" + password + '\'' +
        ", refreshTokenId='" + refreshTokenId + '\'' +
        ", isTokenValid=" + isTokenValid +
        '}';
  }

  public static final class Builder {
    private final String userName;
    private final String password;
    private String refreshTokenId;
    private boolean isTokenValid;

    public Builder(String userName, String password) {
      this.userName = userName;
      this.password = password;
    }

    public Builder isTokenValid(boolean isTokenValid) {
      this.isTokenValid = isTokenValid;

      return this;
    }

    public Builder withRefreshTokenId(String refreshTokenId) {
      this.refreshTokenId = refreshTokenId;

      return this;
    }

    public UserDetails build() {
      return new UserDetails(this);
    }
  }
}
