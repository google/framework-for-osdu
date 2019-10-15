package com.osdu.exception;

public class IngestException extends OsduException {

  public IngestException(String message) {
    super(message);
  }

  public IngestException(String message, Throwable cause) {
    super(message, cause);
  }
}
