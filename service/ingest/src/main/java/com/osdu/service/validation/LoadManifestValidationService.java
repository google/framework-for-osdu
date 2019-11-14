/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.osdu.service.validation;

import static java.lang.String.format;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.ValidationMessage;
import com.osdu.exception.IngestException;
import com.osdu.exception.OsduServerErrorException;
import com.osdu.model.type.manifest.LoadManifest;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoadManifestValidationService {

  private static final String DEFAULT_MANIFEST_SCHEMA_NAME =
      "WorkProductLoadManifestStagedFiles.json";

  final JsonValidationService jsonValidationService;
  final ObjectMapper objectMapper;

  /**
   * Load Manifests no matter what information is in them have to all be validated against general
   * manifest schema.
   *
   * @param loadManifest manifest received with the request
   * @throws IngestException if processing report isn't success.
   */
  public void validateManifest(LoadManifest loadManifest) {
    try {
      log.debug("Start validating load manifest: {}", loadManifest);

      final JsonNode manifestDefaultSchema = getDefaultManifestSchema();

      JsonNode manifest = objectMapper.valueToTree(loadManifest);

      Set<ValidationMessage> errors = jsonValidationService
          .validate(manifestDefaultSchema, manifest);
      log.debug("Validation result: {}", errors);

      if (!errors.isEmpty()) {
        throw new IngestException(format(
            "Failed to validate json from manifest %s, validation result is %s",
            loadManifest, errors));
      }

    } catch (IOException e) {
      throw new IngestException(format("Fail parse load manifest - %s", loadManifest));
    }
  }

  private JsonNode getDefaultManifestSchema() throws IOException {
    log.debug("Fetching default load manifest json schema.");
    InputStream resource = getClass().getClassLoader()
        .getResourceAsStream(DEFAULT_MANIFEST_SCHEMA_NAME);

    if (resource == null) {
      throw new OsduServerErrorException("Can not find resource for load manifest schema");
    }

    return objectMapper.readTree(resource);
  }
}
