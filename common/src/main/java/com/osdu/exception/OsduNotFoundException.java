package com.osdu.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class OsduNotFoundException extends OsduException {

  public OsduNotFoundException(String message) {
    super(message);
  }

  public OsduNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

}