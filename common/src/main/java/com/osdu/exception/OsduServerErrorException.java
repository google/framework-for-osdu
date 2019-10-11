package com.osdu.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class OsduServerErrorException extends OsduException {

  public OsduServerErrorException(String message) {
    super(message);
  }
}