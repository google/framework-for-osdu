package com.osdu.service.delfi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.google.gson.JsonObject;
import com.osdu.exception.IngestException;
import com.osdu.model.IngestResult;
import com.osdu.model.LoadManifest;
import com.osdu.model.SchemaData;
import com.osdu.service.IngestService;
import com.osdu.service.JsonValidationService;
import com.osdu.service.SrnMappingService;
import com.osdu.service.StorageService;
import java.io.IOException;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DelfiIngestService implements IngestService {

  static final String SRN_MANIFEST_KEY = "SRN";

  @Inject
  SrnMappingService srnMappingService;

  @Inject
  StorageService storageService;

  @Inject
  JsonValidationService jsonValidationService;

  @Override
  public IngestResult ingestManifest(LoadManifest loadManifest,
      MessageHeaders headers) {
    //TODO: Split into separate methods, as of now this is just a flow placeholder
    log.info("Request to ingest file with following parameters: {}, and headers : {}", loadManifest,
        headers);

    String schemaSrn = (String) loadManifest.getData().get(SRN_MANIFEST_KEY);

    final SchemaData schemaDataForSrn = srnMappingService.getSchemaDataForSrn(schemaSrn);

    final JsonNode schemaForSrn = storageService
        .getSchemaByLink(schemaDataForSrn.getSchemaLink());

    final JsonNode jsonNodeFromManifest = getJsonNodeFromManifest(loadManifest);
    final ProcessingReport validate = jsonValidationService
        .validate(schemaForSrn, jsonNodeFromManifest);

    if (!validate.isSuccess()) {
      throw new IngestException(String
          .format("Failed to validate json %s against schema %s", jsonNodeFromManifest,
              schemaForSrn));
    }

    //get link to file
    //upload file to delfi
    //submit job
    //poll job for readiness
    //run enrichment
    //create SRN
    //store new SRN

    IngestResult ingestResult = new IngestResult();
    log.info("Request to ingest with parameters : {}, finished with the result: {}", loadManifest,
        ingestResult);
    return ingestResult;
  }

  private JsonNode getJsonNodeFromManifest(LoadManifest loadManifest) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.readTree(mapper.writeValueAsString(loadManifest));
    } catch (IOException e) {
      throw new IngestException(
          String.format("Could not convert object to JSON. Object : %s", loadManifest), e);
    }
  }

  private void extractSrns() {

  }

  private void validateSchema(LoadManifest loadManifest, JsonObject schema) {

  }
}
