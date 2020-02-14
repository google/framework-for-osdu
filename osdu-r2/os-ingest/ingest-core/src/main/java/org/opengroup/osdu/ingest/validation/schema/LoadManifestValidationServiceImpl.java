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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.ValidationMessage;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.ingest.model.SchemaData;
import org.opengroup.osdu.ingest.model.WorkProductLoadManifest;
import org.opengroup.osdu.ingest.provider.interfaces.SchemaRepository;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoadManifestValidationServiceImpl implements LoadManifestValidationService {

  private static final String WP_LOAD_MANIFEST_SCHEMA_TITLE = "WorkProductLoadManifestStagedFiles";

  final ObjectMapper objectMapper;
  final SchemaRepository schemaRepository;
  final JsonValidationService jsonValidationService;

  @Override
  public Set<ValidationMessage> validateManifest(WorkProductLoadManifest loadManifest) {
    log.debug("Start validating load manifest : {}", loadManifest);
    SchemaData schemaData = schemaRepository.findByTitle(WP_LOAD_MANIFEST_SCHEMA_TITLE);
    JsonNode manifest = objectMapper.valueToTree(loadManifest);

    Set<ValidationMessage> errors = jsonValidationService.validate(schemaData.getSchema(), manifest);
    log.debug("Validation result: {}", errors);
    return errors;
  }

}
