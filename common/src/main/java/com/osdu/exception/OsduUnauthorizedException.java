package com.osdu.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class OsduUnauthorizedException extends OsduException {

  public OsduUnauthorizedException(String message) {
    super(message);
  }

  public OsduUnauthorizedException(String message, Throwable cause) {
    super(message, cause);
  }

}