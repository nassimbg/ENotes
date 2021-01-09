package com.enotes.note.service.authentication;

public class TokenInfo {

  private final String id;
  private final String userName;
  private final boolean isValid;
  private final String token;

  public TokenInfo(Builder builder) {
    this.id = builder.id;
    this.userName = builder.userName;
    this.isValid = builder.isValid;
    this.token = builder.token;
  }

  public String getId() {
    return id;
  }

  public String getUserName() {
    return userName;
  }

  public boolean isValid() {
    return isValid;
  }

  public String getToken() {
    return token;
  }

  public static final class Builder {
    private String id;
    private String userName;
    private boolean isValid;
    private String token;

    public Builder(String token) {
      this.token = token;
    }

    public Builder userName(String userName) {
      this.userName = userName;
      return this;
    }

    public Builder id(String id) {
      this.id = id;
      return this;
    }

    public Builder isValid(boolean isValid) {
      this.isValid = isValid;
      return this;
    }

    public TokenInfo build() {
      return new TokenInfo(this);
    }
  }
}
