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

package org.opengroup.osdu.ingest.service;

import static org.assertj.core.api.BDDAssertions.then;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.ValidationMessage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.file.ReplaceCamelCase;
import org.opengroup.osdu.ingest.config.ObjectMapperConfig;
import org.opengroup.osdu.ingest.model.type.file.OsduFile;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
class OsduRecordHelperTest {

  private static final String FILE_LOCATION = "file-location";

  private static OsduRecordHelper osduRecordHelper;
  private static ObjectMapper mapper;

  @BeforeAll
  static void initAll() {
    osduRecordHelper = new OsduRecordHelper();
    ObjectMapperConfig objectMapperConfig = new ObjectMapperConfig();
    mapper = objectMapperConfig.objectMapper();
  }

  @Test
  void shouldCreateValidOsduFileRecord() throws IOException {

    // when
    OsduFile osduFile = osduRecordHelper.populateOsduRecord(FILE_LOCATION);

    // then
    InputStream fileRecordSchema = getFileRecordSchema();
    JsonNode schemaNode = mapper.readTree(fileRecordSchema);
    JsonNode recordNode = mapper.readTree(mapper.writeValueAsString(osduFile));

    Set<ValidationMessage> errors = JsonValidationService.validate(schemaNode, recordNode);
    then(errors).isEmpty();
  }

  private InputStream getFileRecordSchema() {
    return getClass().getClassLoader()
        .getResourceAsStream("OsduFileRecordSchema.json");
  }

}
