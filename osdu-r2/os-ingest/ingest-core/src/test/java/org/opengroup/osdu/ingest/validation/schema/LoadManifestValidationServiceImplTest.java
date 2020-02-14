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

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.BDDMockito.given;
import static org.opengroup.osdu.ingest.ResourceUtils.getResource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.ValidatorTypeCode;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.ingest.ReplaceCamelCase;
import org.opengroup.osdu.ingest.model.SchemaData;
import org.opengroup.osdu.ingest.model.WorkProductLoadManifest;
import org.opengroup.osdu.ingest.provider.interfaces.SchemaRepository;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
class LoadManifestValidationServiceImplTest {

  private static final String SCHEMA_TITLE = "WorkProductLoadManifestStagedFiles";
  private static final String DRAFT_07_SCHEMA_PATH = "3-schemas/TinySchemaDraft7.json";

  private ObjectMapper objectMapper = new ObjectMapper();
  @Mock
  private SchemaRepository schemaRepository;
  @Mock
  private JsonValidationService jsonValidationService;

  private LoadManifestValidationService loadManifestValidationService;

  @BeforeEach
  void setUp() {
    loadManifestValidationService =
        new LoadManifestValidationServiceImpl(objectMapper, schemaRepository, jsonValidationService);
  }

  @Test
  void shouldReturnsEmptyErrorSet() throws Exception {
    // given
    WorkProductLoadManifest loadManifest = WorkProductLoadManifest.builder()
        .build();
    JsonNode schemaJsonNode = objectMapper.readTree(getResource(DRAFT_07_SCHEMA_PATH));
    JsonNode manifestJsonNode = objectMapper.readTree(objectMapper.writeValueAsString(loadManifest));
    given(schemaRepository.findByTitle(SCHEMA_TITLE)).willReturn(
        SchemaData.builder()
            .schema(schemaJsonNode)
            .build());
    given(jsonValidationService.validate(schemaJsonNode, manifestJsonNode))
        .willReturn(Collections.emptySet());

    // when
    Set<ValidationMessage> errors = loadManifestValidationService
        .validateManifest(loadManifest);

    // then
    then(errors).isEmpty();
  }

  @Test
  void shouldReturnsNonEmptyErrorSet() throws Exception {
    // given
    WorkProductLoadManifest loadManifest = WorkProductLoadManifest.builder()
        .build();
    JsonNode schemaJsonNode = objectMapper.readTree(getResource(DRAFT_07_SCHEMA_PATH));
    JsonNode manifestJsonNode = objectMapper.readTree(objectMapper.writeValueAsString(loadManifest));
    ValidationMessage message = ValidationMessage.of("type", ValidatorTypeCode.TYPE, "$.WorkProduct", "null", "object");
    given(schemaRepository.findByTitle(SCHEMA_TITLE)).willReturn(
        SchemaData.builder()
            .schema(schemaJsonNode)
            .build());
    given(jsonValidationService.validate(schemaJsonNode, manifestJsonNode))
        .willReturn(Collections.singleton(message));

    // when
    Set<ValidationMessage> errors = loadManifestValidationService
        .validateManifest(loadManifest);

    // then
    then(errors).hasSize(1);
  }

}
