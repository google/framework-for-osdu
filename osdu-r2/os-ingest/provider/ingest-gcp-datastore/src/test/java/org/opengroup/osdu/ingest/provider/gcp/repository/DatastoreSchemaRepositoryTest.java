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

package org.opengroup.osdu.ingest.provider.gcp.repository;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.opengroup.osdu.ingest.ResourceUtils.getResource;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.ingest.ReplaceCamelCase;
import org.opengroup.osdu.ingest.model.SchemaData;
import org.opengroup.osdu.ingest.provider.gcp.mapper.ISchemaDataMapper;
import org.opengroup.osdu.ingest.provider.gcp.model.entity.SchemaDataEntity;
import org.opengroup.osdu.ingest.provider.interfaces.ISchemaRepository;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
class DatastoreSchemaRepositoryTest {

  private static final String SCHEMA_TITLE = "test-schema-title";
  private static final String DRAFT_07_SCHEMA_PATH = "3-schemas/TinySchemaDraft7.json";

  @Mock
  private ISchemaDataMapper schemaDataMapper;
  @Mock
  private ISchemaDataEntityRepository entityRepository;

  private ObjectMapper objectMapper = new ObjectMapper();

  private ISchemaRepository schemaRepository;

  @BeforeEach
  void setUp() {
    schemaRepository = new DatastoreSchemaRepository(schemaDataMapper, entityRepository);
  }

  @Test
  void shouldFindSchemaDataByTitle() throws Exception {
    // given
    Date now = new Date();
    given(entityRepository.findByTitle(SCHEMA_TITLE)).willReturn(SchemaDataEntity.builder()
        .title(SCHEMA_TITLE)
        .schema(getResource(DRAFT_07_SCHEMA_PATH))
        .createdAt(now)
        .build());
    given(schemaDataMapper.schemaDataDtoToSchemaData(any())).willAnswer(invocation -> {
      SchemaDataEntity entity = invocation.getArgument(0);
      return SchemaData.builder()
          .title(entity.getTitle())
          .schema(objectMapper.readTree(entity.getSchema()))
          .created(now)
          .build();
    });

    // when
    SchemaData schemaData = schemaRepository.findByTitle(SCHEMA_TITLE);

    // then
    then(schemaData).isEqualTo(SchemaData.builder()
        .title(SCHEMA_TITLE)
        .schema(objectMapper.readTree(getResource(DRAFT_07_SCHEMA_PATH)))
        .created(now)
        .build());
  }

  @Test
  void shouldReturnNullWhenNothingWasFound() {
    // when
    SchemaData schemaData = schemaRepository.findByTitle("nothing");

    // then
    then(schemaData).isNull();
  }

}
