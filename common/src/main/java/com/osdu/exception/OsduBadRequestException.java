package com.osdu.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class OsduBadRequestException extends OsduException {

  public OsduBadRequestException(String message) {
    super(message);
  }

  public OsduBadRequestException(String message, Throwable cause) {
    super(message, cause);
  }

}