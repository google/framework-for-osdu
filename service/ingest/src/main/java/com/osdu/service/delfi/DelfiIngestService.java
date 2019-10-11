package com.osdu.service.delfi;

import static com.osdu.service.JsonUtils.getJsonNode;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.osdu.client.DelfiEntitlementsClient;
import com.osdu.client.DelfiIngestionClient;
import com.osdu.exception.IngestException;
import com.osdu.model.IngestResult;
import com.osdu.model.manifest.LoadManifest;
import com.osdu.model.property.DelfiPortalProperties;
import com.osdu.service.IngestService;
import com.osdu.service.JobStatusService;
import com.osdu.service.JsonValidationService;
import com.osdu.service.processing.InnerInjectionProcess;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DelfiIngestService implements IngestService {

  static final String DEFAULT_MANIFEST_SCHEMA_NAME = "LoadManifestSchema.json";

  final DelfiPortalProperties portalProperties;

  final ObjectMapper objectMapper;

  final DelfiIngestionClient delfiIngestionClient;
  final DelfiEntitlementsClient delfiEntitlementsClient;

  final JsonValidationService jsonValidationService;
  final JobStatusService jobStatusService;
  final InnerInjectionProcess innerInjectionProcess;

  @Override
  public IngestResult ingestManifest(LoadManifest loadManifest,
      MessageHeaders headers) {
    log.info("Request to ingest file with following parameters: {}, and headers : {}", loadManifest,
        headers);

    final ProcessingReport validationResult = validateManifest(loadManifest);

    if (!validationResult.isSuccess()) {
      throw new IngestException(String
          .format("Failed to validate json from manifest %s, validation result is %s", loadManifest,
              validationResult));
    }

    String jobId = jobStatusService.initInjectJob();

    innerInjectionProcess.process(jobId, loadManifest, headers);

    log.info("Request to ingest with parameters : {}, init the injection jobId: {}", loadManifest,
        jobId);
    return IngestResult.builder()
        .jobId(jobId)
        .build();
  }

  private ProcessingReport validateManifest(LoadManifest loadManifest) {
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
