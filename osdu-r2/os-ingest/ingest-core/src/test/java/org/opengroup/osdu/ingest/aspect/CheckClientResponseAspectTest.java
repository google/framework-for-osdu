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

package org.opengroup.osdu.ingest.aspect;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.opengroup.osdu.ingest.TestUtils.getFeignRequest;

import feign.Response;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.exception.OsduBadRequestException;
import org.opengroup.osdu.core.common.exception.OsduException;
import org.opengroup.osdu.core.common.exception.OsduNotFoundException;
import org.opengroup.osdu.core.common.exception.OsduUnauthorizedException;
import org.opengroup.osdu.ingest.ReplaceCamelCase;
import org.opengroup.osdu.ingest.exception.OsduForbiddenException;
import org.opengroup.osdu.ingest.exception.OsduServerErrorException;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
public class CheckClientResponseAspectTest {

  private static final String HTTP_TEST_URL = "http://test-url";

  CheckClientResponseAspect aspect;

  @BeforeEach
  void setUp() {
    aspect = new CheckClientResponseAspect();
  }

  @Test
  void shouldPassIfStatusIsOk() {

    // given
    Response response = Response.builder()
        .body("response body", StandardCharsets.UTF_8)
        .request(getFeignRequest())
        .status(200).build();

    // when
    Throwable thrown = catchThrowable(() -> aspect.checkResponse(response));

    // then
    then(thrown).isNull();
  }

  @ParameterizedTest(name = "{index} ==> Should decode HTTP status {0}")
  @MethodSource("org.opengroup.osdu.ingest.aspect.CheckClientResponseAspectTest#filesListRequestProvider")
  void shouldDecodeFeignExceptions(ArgumentsAccessor arguments) {

    // given
    Response response = Response.builder()
        .body(arguments.getString(1), StandardCharsets.UTF_8)
        .request(getFeignRequest())
        .status(arguments.getInteger(0)).build();

    // when
    Throwable thrown = catchThrowable(() -> aspect.checkResponse(response));

    // then
    then(thrown).isInstanceOf(arguments.get(2, Class.class));
    then(thrown.getMessage()).isEqualTo(arguments.getString(1));
  }

  static Stream<Arguments> filesListRequestProvider() {

    return Stream.of(
        arguments(400, "Bad request", OsduBadRequestException.class),
        arguments(401, "Unauthorized", OsduUnauthorizedException.class),
        arguments(403, "Bad Forbidden", OsduForbiddenException.class),
        arguments(404, "Not found", OsduNotFoundException.class),
        arguments(408, "Request timeout", OsduException.class),
        arguments(500, "Internal server error", OsduServerErrorException.class)
    );
  }

}
