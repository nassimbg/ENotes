package com.enotes.note.application.exception;

import com.enotes.note.service.authentication.AlreadyExistsException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class AlreadyExistsExceptionHandler extends AbstractExceptionHandler {

  @ExceptionHandler(value = {AlreadyExistsException.class})
  protected ResponseEntity<Object> handleConflict(
      RuntimeException ex, WebRequest request) {
    return super.handleConflict(ex, request);
  }

  @Override
  public HttpStatus getResponseStatus() {
    return HttpStatus.CONFLICT;
  }
}
