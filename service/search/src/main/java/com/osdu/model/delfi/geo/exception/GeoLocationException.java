package com.osdu.model.delfi.geo.exception;


import com.osdu.exception.SearchException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class GeoLocationException extends SearchException {

  public GeoLocationException(String message) {
    super(message);
  }
}
