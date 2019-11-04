package com.osdu.service.validation;

import static com.osdu.service.JsonUtils.getJsonNode;

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
   */
  public ProcessingReport validateManifest(LoadManifest loadManifest) {
    log.debug("Start validating load manifest: {}", loadManifest);
    final JsonNode manifestDefaultSchema = getDefaultManifestSchema();

    final JsonNode jsonNodeFromManifest = getJsonNode(loadManifest);

    ProcessingReport report = jsonValidationService.validate(manifestDefaultSchema,
        jsonNodeFromManifest);
    log.debug("Validation report: {}", report);
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
