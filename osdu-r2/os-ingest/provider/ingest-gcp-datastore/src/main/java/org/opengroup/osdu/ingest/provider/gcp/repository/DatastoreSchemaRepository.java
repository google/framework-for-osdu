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

package org.opengroup.osdu.ingest.provider.gcp.repository;

import javax.inject.Named;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.ingest.model.SchemaData;
import org.opengroup.osdu.ingest.provider.gcp.mapper.SchemaDataMapper;
import org.opengroup.osdu.ingest.provider.gcp.model.entity.SchemaDataEntity;
import org.opengroup.osdu.ingest.provider.interfaces.SchemaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
@RequiredArgsConstructor
public class DatastoreSchemaRepository implements SchemaRepository {

  @Named
  final SchemaDataMapper schemaDataMapper;
  final SchemaDataEntityRepository schemaDataEntityRepository;

  @Override
  public SchemaData findByTitle(String title) {
    log.debug("Requesting schema data. Schema title : {}", title);
    SchemaDataEntity entity = schemaDataEntityRepository.findByTitle(title);
    SchemaData schemaData = schemaDataMapper.schemaDataDtoToSchemaData(entity);
    log.debug("Found schema data : {}", schemaData);
    return schemaData;
  }

}
