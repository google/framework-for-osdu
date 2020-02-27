/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.workflow.exception.handler;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import java.util.ArrayList;
import java.util.List;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
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

  @ExceptionHandler({ JsonParseException.class, IllegalStateException.class,
      MismatchedInputException.class })
  protected ResponseEntity<Object> handleInvalidBody(RuntimeException ex,
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

  @ExceptionHandler({ ConstraintViolationException.class })
  protected ResponseEntity<Object> handle(ConstraintViolationException ex, WebRequest request) {
    List<String> errors = new ArrayList<>();
    for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
      errors.add(violation.getPropertyPath() + ": " + violation.getMessage());
    }
    log.error("Constraint exception: {}", errors);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    ApiError apiError = ApiError.builder()
        .status(HttpStatus.BAD_REQUEST)
        .message(ExceptionUtils.getRootCauseMessage(ex))
        .errors(errors)
        .build();
    return handleExceptionInternal(ex, apiError, headers, HttpStatus.BAD_REQUEST, request);
  }

}
