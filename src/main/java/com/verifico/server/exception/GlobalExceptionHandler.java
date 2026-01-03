package com.verifico.server.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex){
    ErrorResponse errorResponse = new ErrorResponse(LocalDateTime.now(), ex.getStatusCode().value(), ex.getStatusCode().toString(), ex.getReason());
    return new ResponseEntity<ErrorResponse>(errorResponse,ex.getStatusCode());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception ex){
    ErrorResponse errorResponse = new ErrorResponse(LocalDateTime.now(),500, "Internal Server Error", ex.getMessage());
    return new ResponseEntity<ErrorResponse>(errorResponse,HttpStatus.INTERNAL_SERVER_ERROR);
  }

}
