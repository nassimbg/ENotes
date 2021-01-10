package com.enotes.note.service.notes;

public class NotesException extends RuntimeException {

  private static final long serialVersionUID = 1762196437438739628L;

  public NotesException(String message) {
    super(message);
  }

  public NotesException(String message, Throwable cause) {
    super(message, cause);
  }

  public NotesException(Throwable cause) {
    super(cause);
  }
}
