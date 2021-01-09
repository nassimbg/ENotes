package com.enotes.note.service.authentication;

public class AlreadyExistsException extends RuntimeException {

  private static final long serialVersionUID = 1762196437438739628L;

  public AlreadyExistsException(String message) {
    super(message);
  }

  public AlreadyExistsException(String message, Throwable cause) {
    super(message, cause);
  }
}
