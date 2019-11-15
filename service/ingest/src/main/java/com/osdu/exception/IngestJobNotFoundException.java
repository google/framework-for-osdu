package com.osdu.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class IngestJobNotFoundException extends OsduException {

  public IngestJobNotFoundException(String message) {
    super(message);
  }

  public IngestJobNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

}
