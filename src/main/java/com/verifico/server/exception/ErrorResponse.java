package com.verifico.server.exception;

import java.time.LocalDateTime;

public class ErrorResponse {
  private LocalDateTime timeStamp;
  private int statusCode;
  private String error;
  private String message;

  public ErrorResponse(LocalDateTime timeStamp, int statusCode, String error, String message) {
    this.timeStamp = timeStamp;
    this.statusCode = statusCode;
    this.error = error;
    this.message = message;
  }

  public LocalDateTime getTimeStamp(){
    return timeStamp;
  }

  public void setTimeStamp(LocalDateTime timeStamp){
    this.timeStamp = timeStamp;
  }

  public int getStatusCode(){
    return statusCode;
  }

  public void setStatusCode(int statusCode){
    this.statusCode = statusCode;
  }

  public String getError(){
    return error;
  }

  public void setError(String error){
    this.error = error;
  }

  public String getMessage(){
    return message;
  }

  public void setMessage(String message){
    this.message = message;
  }

}
