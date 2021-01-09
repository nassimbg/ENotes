package com.enotes.note.application.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

public abstract class AbstractExceptionHandler extends ResponseEntityExceptionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger("HTTP");


  protected ResponseEntity<Object> handleConflict(
      RuntimeException ex, WebRequest request) {
    LOGGER.error("error occurred", ex);

    String message = ex.getMessage() == null ? "" : ex.getMessage();

    return handleExceptionInternal(ex, message, new HttpHeaders(), getResponseStatus(), request);
  }

  public abstract HttpStatus getResponseStatus();
}
