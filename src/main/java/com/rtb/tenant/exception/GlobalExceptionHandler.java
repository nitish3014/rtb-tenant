package com.rtb.tenant.exception;

import com.rtb.tenant.utls.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ApiErrorResponse> handleUnauthorizedException(
      UnauthorizedException e
  ) {
    String response = e.getMessage();
    ApiErrorResponse apiErrorResponse = new ApiErrorResponse(response, "UNAUTHORIZED");
    return new ResponseEntity<>(apiErrorResponse, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(
      ResourceNotFoundException e
  ) {
    String response = e.getMessage();
    ApiErrorResponse apiErrorResponse = new ApiErrorResponse(response, "NOT_FOUND");
    return new ResponseEntity<>(apiErrorResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(InternalServerErrorException.class)
  public ResponseEntity<ApiErrorResponse> handleInternalServerErrorException(
          InternalServerErrorException e) {
    String response = e.getMessage();
    ApiErrorResponse apiErrorResponse = new ApiErrorResponse(response, "INTERNAL_SERVER_EXCEPTION");
    return new ResponseEntity<>(apiErrorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex) {
    ApiErrorResponse response = new ApiErrorResponse(
            "An unexpected error occurred", "UNKNOWN_ERROR");
    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiErrorResponse> handleBadRequest(IllegalArgumentException e) {
    String response = e.getMessage();
    ApiErrorResponse apiErrorResponse = new ApiErrorResponse(response, "BAD_REQUEST");
    return new ResponseEntity<>(apiErrorResponse, HttpStatus.BAD_REQUEST);
  }

}
