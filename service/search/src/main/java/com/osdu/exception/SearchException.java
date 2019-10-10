package com.osdu.exception;

public class SearchException extends OsduException {

  public SearchException(String message) {
    super(message);
  }

  public SearchException(String message, Throwable cause) {
    super(message, cause);
  }
}
