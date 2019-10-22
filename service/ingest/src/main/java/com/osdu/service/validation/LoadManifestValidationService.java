package com.osdu.service.validation;

import static com.osdu.service.JsonUtils.getJsonNode;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.osdu.exception.IngestException;
import com.osdu.model.manifest.LoadManifest;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoadManifestValidationService {

  private static final String DEFAULT_MANIFEST_SCHEMA_NAME = "LoadManifestSchema.json";

  final JsonValidationService jsonValidationService;
  final ObjectMapper objectMapper;

  public ProcessingReport validateManifest(LoadManifest loadManifest) {
    final JsonNode manifestDefaultSchema = getDefaultManifestSchema();

    final JsonNode jsonNodeFromManifest = getJsonNode(loadManifest);

    return jsonValidationService.validate(manifestDefaultSchema, jsonNodeFromManifest);
  }

  private JsonNode getDefaultManifestSchema() {
    try {
      return objectMapper.readTree(getClass().getClassLoader()
          .getResource(DEFAULT_MANIFEST_SCHEMA_NAME));
    } catch (IOException e) {
      throw new IngestException("Failed to get default schema for load manifest", e);
    }
  }
}
