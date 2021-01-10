package com.enotes.note.application.authentication;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "store")
public class StoreConfigProperties {

  private String location;
  private String pwd;
  private String keyPwd;

  public StoreConfigProperties() {
    //do nothing
  }

  private StoreConfigProperties(final Builder builder) {
    this.location = builder.location;
    this.pwd = builder.pwd;
    this.keyPwd = builder.keyPwd;
  }

  public String getLocation() {
    return location;
  }

  public String getPwd() {
    return pwd;
  }

  public String getKeyPwd() {
    return keyPwd;
  }

  public void setLocation(final String location) {
    this.location = location;
  }

  public void setPwd(final String pwd) {
    this.pwd = pwd;
  }

  public void setKeyPwd(final String keyPwd) {
    this.keyPwd = keyPwd;
  }

  public static class Builder {
    private String location;
    private String pwd;
    private String keyPwd;

    public Builder withLocation(String location) {
      this.location = location;

      return this;
    }

    public Builder withPwd(String pwd) {
      this.pwd = pwd;

      return this;
    }

    public Builder withKeyPwd(String keyPwd) {
      this.keyPwd = keyPwd;

      return this;
    }

    public StoreConfigProperties build() {
      return new StoreConfigProperties(this);
    }
  }
}
