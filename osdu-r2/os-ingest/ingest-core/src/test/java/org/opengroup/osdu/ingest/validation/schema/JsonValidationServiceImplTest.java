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

package org.opengroup.osdu.ingest.validation.schema;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.opengroup.osdu.ingest.ResourceUtils.getResource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.ingest.ReplaceCamelCase;
import org.opengroup.osdu.ingest.exception.ServerErrorException;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
class JsonValidationServiceImplTest {

  private static final String DRAFT_07_SCHEMA_PATH = "3-schemas/TinySchemaDraft7.json";
  private static final String DRAFT_06_SCHEMA_PATH = "3-schemas/TinySchemaDraft6.json";

  private ObjectMapper objectMapper = new ObjectMapper();
  private IJsonValidationService jsonValidationService = new JsonValidationServiceImpl();

  @Test
  void shouldValidateObjectByProvidedSchema() throws Exception {
    // given
    JsonNode schema = objectMapper.readTree(getResource(DRAFT_07_SCHEMA_PATH));
    JsonNode object = objectMapper.readTree("{}");

    // when
    Throwable thrown = catchThrowable(() -> jsonValidationService.validate(schema, object));

    // then
    then(thrown).isNull();
  }

  @Test
  void shouldThrownExceptionWhenUnableToValidateBySchema() throws Exception {
    // given
    JsonNode schema = objectMapper.readTree(getResource(DRAFT_06_SCHEMA_PATH));
    JsonNode object = objectMapper.readTree("{}");

    // when
    Throwable thrown = catchThrowable(() -> jsonValidationService.validate(schema, object));

    // then
    then(thrown)
        .isInstanceOf(ServerErrorException.class)
        .hasMessageMatching("Error creating json validation schema from json object: (.*)");
  }

}
