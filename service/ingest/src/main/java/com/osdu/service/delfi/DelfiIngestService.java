package com.osdu.service.delfi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.google.gson.JsonObject;
import com.osdu.exception.IngestException;
import com.osdu.model.IngestResult;
import com.osdu.model.SchemaData;
import com.osdu.model.manifest.File;
import com.osdu.model.manifest.LoadManifest;
import com.osdu.service.IngestService;
import com.osdu.service.JsonValidationService;
import com.osdu.service.SrnMappingService;
import com.osdu.service.StorageService;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
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
    log.info("Request to ingest file with following parameters: {}, and headers : {}", loadManifest,
        headers);

    final ProcessingReport validationResult = validateManifest(loadManifest);

    if (!validationResult.isSuccess()) {
      throw new IngestException(String
          .format("Failed to validate json from manifest %s, validation result is %s", loadManifest,
              validationResult));
    }

    //get link to file
    final List<URL> fileUrls = getFileUrls(loadManifest);
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

  private ProcessingReport validateManifest(LoadManifest loadManifest) {
    String schemaSrn = getSchemaSrn(loadManifest);

    final SchemaData schemaDataForSrn = srnMappingService.getSchemaDataForSrn(schemaSrn);

    final JsonNode schemaForSrn = storageService
        .getSchemaByLink(schemaDataForSrn.getSchemaLink());
    final JsonNode jsonNodeFromManifest = getJsonNodeFromManifest(loadManifest);

    return jsonValidationService
        .validate(schemaForSrn, jsonNodeFromManifest);


  }

  private String getSchemaSrn(LoadManifest loadManifest) {
    return (String) loadManifest.getWorkProduct().get(SRN_MANIFEST_KEY);
  }

  private List<URL> getFileUrls(LoadManifest loadManifest) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      final List<File> fileList = mapper
          .readValue(mapper.writeValueAsString(loadManifest.getFiles()),
              new TypeReference<List<File>>() {
              });
      return fileList.stream().map(file -> {
        try {
          return new URL(file.getData().getGroupTypeProperties().getStagingFilePath());
        } catch (MalformedURLException e) {
          throw new IngestException(
              String.format("Could not create URL from staging link : %s",
                  file.getData().getGroupTypeProperties().getStagingFilePath()),
              e);
        }
      }).collect(Collectors.toList());
    } catch (IOException e) {
      throw new IngestException(
          String.format("Could not convert object to JSON. Object : %s", loadManifest.getFiles()),
          e);
    }
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
