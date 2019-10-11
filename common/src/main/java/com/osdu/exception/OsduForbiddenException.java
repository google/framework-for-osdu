package com.osdu.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class OsduForbiddenException extends OsduException {

  public OsduForbiddenException(String message) {
    super(message);
  }
}