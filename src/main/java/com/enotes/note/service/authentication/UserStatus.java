package com.enotes.note.service.authentication;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class UserStatus {

  private final String userName;
  private boolean logOut;

  private UserStatus() {
    this.userName = null;
  }

  public UserStatus(final String userName, final boolean logOut) {
    this.userName = userName;
    this.logOut = logOut;
  }

  @JsonProperty(value="userName")
  public String getUserName() {
    return userName;
  }

  @JsonProperty(value="logOut")
  public boolean logOut() {
    return logOut;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final UserStatus that = (UserStatus) o;
    return logOut == that.logOut && Objects.equals(userName, that.userName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userName, logOut);
  }

  @Override
  public String toString() {
    return "UserStatus{" +
        "userName='" + userName + '\'' +
        ", logOut=" + logOut +
        '}';
  }
}
