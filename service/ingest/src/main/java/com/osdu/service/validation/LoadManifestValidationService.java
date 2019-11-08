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

import static com.osdu.service.JsonUtils.getJsonNode;
import static java.lang.String.format;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.osdu.exception.IngestException;
import com.osdu.model.manifest.LoadManifest;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoadManifestValidationService {

  private static final String DEFAULT_MANIFEST_SCHEMA_NAME = "LoadManifestSchema.json";

  final JsonValidationService jsonValidationService;
  final ObjectMapper objectMapper;

  /**
   * Load Manifests no matter what information is in them have to all be validated against general
   * manifest schema.
   *
   * @param loadManifest manifest received with the request
   * @return report with the result and a list of errors and warnings (if any)
   * @throws IngestException if processing report isn't success.
   */
  public ProcessingReport validateManifest(LoadManifest loadManifest) {
    log.debug("Start validating load manifest: {}", loadManifest);
    final JsonNode manifestDefaultSchema = getDefaultManifestSchema();

    final JsonNode jsonNodeFromManifest = getJsonNode(loadManifest);

    ProcessingReport report = jsonValidationService.validate(manifestDefaultSchema,
        jsonNodeFromManifest);
    log.debug("Validation report: {}", report);

    if (!report.isSuccess()) {
      throw new IngestException(
          format("Failed to validate json from manifest %s, validation result is %s", loadManifest,
              report));
    }

    return report;
  }

  private JsonNode getDefaultManifestSchema() {
    try {
      log.debug("Fetching default load manifest json schema.");
      return objectMapper.readTree(getClass().getClassLoader()
          .getResource(DEFAULT_MANIFEST_SCHEMA_NAME));
    } catch (IOException e) {
      throw new IngestException("Failed to get default schema for load manifest", e);
    }
  }
}
