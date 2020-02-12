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

package org.opengroup.osdu.ingest.provider.gcp.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.ingest.model.SchemaData;
import org.opengroup.osdu.ingest.provider.gcp.exception.SchemaMappingException;
import org.opengroup.osdu.ingest.provider.gcp.model.dto.SchemaDataDto;

@Slf4j
public abstract class SchemaDataMapperDecorator implements SchemaDataMapper {

  @Inject
  @Named("org.opengroup.osdu.ingest.provider.gcp.mapper.SchemaDataMapperImpl_")
  SchemaDataMapper schemaDataMapper;
  @Inject
  ObjectMapper objectMapper;

  @Override
  public SchemaData schemaDataDtoToSchemaData(SchemaDataDto schemaDataDto) {
    log.debug("Request to map schemaDataDto to schemaData : {} ", schemaDataDto);
    SchemaData schemaData = schemaDataMapper.schemaDataDtoToSchemaData(schemaDataDto);

    try {
      schemaData.setSchema(objectMapper.readTree(schemaDataDto.getSchema()));
      log.debug("Result of mapping: {} ", schemaData);
      return schemaData;
    } catch (JsonProcessingException e) {
      throw new SchemaMappingException(
          String.format("Failed to map schemaData to schemaDataDto : %s", schemaData), e);
    }
  }
}
