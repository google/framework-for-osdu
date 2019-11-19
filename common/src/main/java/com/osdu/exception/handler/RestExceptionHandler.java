package com.osdu.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Slf4j
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(IllegalArgumentException.class)
  protected ResponseEntity<Object> handleInvalidBody(IllegalArgumentException ex,
      WebRequest request) {
    log.error("Exception during REST request: " + request.getDescription(false), ex);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    ApiError apiError = ApiError.builder()
        .status(HttpStatus.BAD_REQUEST)
        .message(ExceptionUtils.getRootCauseMessage(ex))
        .build();
    return handleExceptionInternal(ex, apiError, headers,
        HttpStatus.BAD_REQUEST, request);
  }

}
