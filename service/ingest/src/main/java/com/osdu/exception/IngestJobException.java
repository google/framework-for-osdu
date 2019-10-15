package com.osdu.exception;

public class IngestJobException extends OsduException {

  public IngestJobException(String message) {
    super(message);
  }

  public IngestJobException(String message, Throwable cause) {
    super(message, cause);
  }
}
