package com.enotes.note.service.authentication;

public class AuthenticationException extends RuntimeException {

  private static final long serialVersionUID = 1762196437438739628L;

  public AuthenticationException(String message) {
    super(message);
  }

  public AuthenticationException(String message, Throwable cause) {
    super(message, cause);
  }

  public AuthenticationException(Throwable cause) {
    super(cause);
  }
}
