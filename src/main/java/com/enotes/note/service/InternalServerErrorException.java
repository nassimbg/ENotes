package com.enotes.note.service;

public class InternalServerErrorException extends RuntimeException {

  private static final long serialVersionUID = 1762196437438739628L;

  public InternalServerErrorException(String message) {
    super(message);
  }

  public InternalServerErrorException(String message, Throwable cause) {
    super(message, cause);
  }

  public InternalServerErrorException(Throwable cause) {
    super(cause);
  }
}
