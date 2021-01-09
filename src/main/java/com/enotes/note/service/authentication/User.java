package com.enotes.note.service.authentication;

import java.util.Objects;

public final class User {

  private final String userName;
  private final String password;

  private User() {
    userName = null;
    password = null;
  }

  private User(final Builder builder) {
    userName = builder.userName;
    password = builder.password;
  }

  public String getUserName() {
    return userName;
  }

  public String getPassword() {
    return password;
  }

  public Builder toBuilder() {
    return new Builder(userName)
        .withPassword(password);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final User user = (User) o;
    return Objects.equals(userName, user.userName) &&
        Objects.equals(password, user.password);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userName, password);
  }

  @Override
  public String toString() {
    return "User{" +
        "userName='" + userName + '\'' +
        ", password='" + password + '\'' +
        '}';
  }

  public static Builder builder(String userName) {
    return new Builder(userName);
  }

  public static final class Builder {
    private final String userName;
    private String password;

    private Builder(String userName) {
      this.userName = userName;
    }

    public Builder withPassword(String password) {
      this.password = password;
      return this;
    }

    public User build() {
      return new User(this);
    }
  }
}
