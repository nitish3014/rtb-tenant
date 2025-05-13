package com.rtb.tenant.exception;

public class BadRequestException extends RuntimeException {

  public BadRequestException(String message, Throwable ex) {
    super(message, ex);
  }

  public BadRequestException(String message) {
    super(message);
  }

  public BadRequestException(Throwable ex) {
    super(ex);
  }
}
