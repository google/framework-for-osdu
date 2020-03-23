/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.ingest.exception.handler;

import static org.assertj.core.api.BDDAssertions.then;
import static org.hibernate.validator.internal.engine.path.PathImpl.createPathFromString;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.BDDMockito.given;

import com.google.common.collect.Sets;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.ingest.ReplaceCamelCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
class RestExceptionHandlerTest {

  @Mock
  private WebRequest webRequest;
  @Mock
  private ConstraintViolation<String> constraintViolation;
  @Mock
  private JaxRsDpsLog logger;

  RestExceptionHandler restExceptionHandler;

  @BeforeEach
  void setUp() {
    restExceptionHandler = new RestExceptionHandler(logger);
  }

  @Test
  void shouldHandleIllegalArgumentException() {

    // given
    given(webRequest.getDescription(anyBoolean())).willReturn("uri=/test");

    // when
    ResponseEntity<Object> response = restExceptionHandler
        .handleInvalidBody(new IllegalArgumentException("Cannot convert JSON"), webRequest);

    // then
    then(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    then(response.getBody()).satisfies(body -> {
      ApiError error = (ApiError) body;
      then(error.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
      then(error.getMessage()).isEqualTo("IllegalArgumentException: Cannot convert JSON");
    });
  }

  @Test
  void shouldHandleConstraintException() {

    // given
    ConstraintViolationException constraintViolationException = new ConstraintViolationException(
        Sets.newHashSet(constraintViolation));

    given(constraintViolation.getPropertyPath()).willReturn(createPathFromString("testField"));
    given(constraintViolation.getMessage()).willReturn("testMessage");

    // when
    ResponseEntity<Object> response = restExceptionHandler
        .handle(constraintViolationException, webRequest);

    // then
    then(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    then(response.getBody()).satisfies(body -> {
      ApiError error = (ApiError) body;
      then(error.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
      then(error.getMessage()).contains("ConstraintViolationException");
      then(error.getErrors().get(0)).isEqualTo("testField: testMessage");
    });
  }
}
