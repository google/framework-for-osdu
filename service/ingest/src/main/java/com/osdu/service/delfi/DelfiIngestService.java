package com.osdu.service.delfi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.google.cloud.storage.Blob;
import com.osdu.client.DelfiIngestionClient;
import com.osdu.exception.IngestException;
import com.osdu.model.IngestResult;
import com.osdu.model.SchemaData;
import com.osdu.model.delfi.SignedUrlResult;
import com.osdu.model.manifest.File;
import com.osdu.model.manifest.LoadManifest;
import com.osdu.service.IngestService;
import com.osdu.service.JsonValidationService;
import com.osdu.service.SrnMappingService;
import com.osdu.service.StorageService;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DelfiIngestService implements IngestService {

  static final String SRN_MANIFEST_KEY = "SRN";
  static final String PARTITION_HEADER_KEY = "partition";
  static final String AUTHORIZATION_HEADER_KEY = "authorization";

  @Inject
  SrnMappingService srnMappingService;

  @Inject
  StorageService storageService;

  @Inject
  JsonValidationService jsonValidationService;

  @Inject
  DelfiIngestionClient delfiIngestionClient;

  @Value("${osdu.delfi.portal.appkey}")
  String appKey;

  @Override
  public IngestResult ingestManifest(LoadManifest loadManifest,
      MessageHeaders headers) {
    log.info("Request to ingest file with following parameters: {}, and headers : {}", loadManifest,
        headers);

    final ProcessingReport validationResult = validateManifest(loadManifest);

    String authorizationToken = extractHeaderByName(headers, AUTHORIZATION_HEADER_KEY);
    String partition = extractHeaderByName(headers, PARTITION_HEADER_KEY);

    if (!validationResult.isSuccess()) {
      throw new IngestException(String
          .format("Failed to validate json from manifest %s, validation result is %s", loadManifest,
              validationResult));
    }

    //get link to file
    final List<URL> fileUrls = getFileUrls(loadManifest);

    List<URL> resultFilesUrls = fileUrls.stream()
        .map(url -> transferFile(url, authorizationToken, partition)).collect(Collectors.toList());

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

    final JsonNode jsonNodeFromManifest = getJsonNodeFromManifest(loadManifest);

    return jsonValidationService
        .validate(schemaDataForSrn.getSchema(), jsonNodeFromManifest);


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

  private URL transferFile(URL fileUrl, String authToken, String partition) {
    String filename = getFileNameFromUrl(fileUrl);
    Blob blob = storageService.uploadFileToStorage(fileUrl, filename);
    URL delfiSignedUrl = getDelfiSignedUrl(filename, authToken, partition);

    return storageService.writeFileToSignedUrlLocation(blob, delfiSignedUrl);
  }

  private String extractHeaderByName(MessageHeaders headers, String headerKey) {
    log.debug("Extracting header with name : {} from map : {}", headerKey, headers);
    if (headers.containsKey(headerKey)) {
      String result = (String) headers.get(headerKey);
      log.debug("Found header in the request with following key:value pair : {}:{}", headerKey,
          result);
      return result;
    }
    return null;
  }

  private URL getDelfiSignedUrl(String fileName, String authToken, String partition) {
    SignedUrlResult signedUrlForLocation = delfiIngestionClient
        .getSignedUrlForLocation(fileName, authToken, appKey, partition, partition);

    return signedUrlForLocation.getLocationUrl();
  }

  private String getFileNameFromUrl(URL fileUrl) {
    try {
      return Paths.get(new URI(fileUrl.toString()).getPath()).getFileName().toString();
    } catch (URISyntaxException e) {
      throw new IngestException(String.format("Can not get file name from URL: %s", fileUrl), e);
    }
  }
}
