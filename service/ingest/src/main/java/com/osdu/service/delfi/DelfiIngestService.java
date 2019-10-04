package com.osdu.service.delfi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.google.cloud.storage.Blob;
import com.osdu.client.DelfiEntitlementsClient;
import com.osdu.client.DelfiIngestionClient;
import com.osdu.exception.IngestException;
import com.osdu.model.IngestResult;
import com.osdu.model.Record;
import com.osdu.model.SchemaData;
import com.osdu.model.delfi.RequestMeta;
import com.osdu.model.delfi.SignedUrlResult;
import com.osdu.model.delfi.entitlement.Group;
import com.osdu.model.delfi.submit.SubmitJobResult;
import com.osdu.model.manifest.File;
import com.osdu.model.manifest.GroupTypeProperties;
import com.osdu.model.manifest.LoadManifest;
import com.osdu.model.manifest.ManifestFields;
import com.osdu.model.property.DelfiPortalProperties;
import com.osdu.request.OsduHeader;
import com.osdu.service.EnrichService;
import com.osdu.service.IngestService;
import com.osdu.service.JsonValidationService;
import com.osdu.service.SrnMappingService;
import com.osdu.service.StorageService;
import com.osdu.service.SubmitService;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

  final SrnMappingService srnMappingService;
  final StorageService storageService;
  final JsonValidationService jsonValidationService;
  final EnrichService enrichService;
  final SubmitService submitService;

  @Override
  public IngestResult ingestManifest(LoadManifest loadManifest,
      MessageHeaders headers) {
    log.info("Request to ingest file with following parameters: {}, and headers : {}", loadManifest,
        headers);

    SchemaData schemaData = getSchemaData(loadManifest);
    final ProcessingReport validationResult = validateManifest(loadManifest);

    String authorizationToken = extractHeaderByName(headers, OsduHeader.AUTHORIZATION);
    String partition = normalizePartition(extractHeaderByName(headers, OsduHeader.PARTITION));
    String legalTags = extractHeaderByName(headers, OsduHeader.LEGAL_TAGS);

    if (!validationResult.isSuccess()) {
      throw new IngestException(String
          .format("Failed to validate json from manifest %s, validation result is %s", loadManifest,
              validationResult));
    }

    Map<String, String> groupEmailByName = delfiEntitlementsClient
        .getUserGroups(authorizationToken, portalProperties.getAppKey(), partition)
        .getGroups().stream()
        .collect(Collectors.toMap(Group::getName, Group::getEmail));

    RequestMeta requestMeta = RequestMeta.builder()
        .appKey(portalProperties.getAppKey())
        .authorizationToken(authorizationToken)
        .partition(partition)
        .legalTags(legalTags)
        .schemaData(schemaData)
        .userGroupEmailByName(groupEmailByName)
        .build();

    //get link to file
    final List<URL> fileUrls = getFileUrls(loadManifest);

    List<String> relativeFilePaths = fileUrls.stream()
        .map(url -> transferFile(url, authorizationToken, partition))
        .collect(Collectors.toList());

    //submit job
    List<SubmitJobResult> submitResults = relativeFilePaths.stream()
        .map(path -> submitService.submitFile(path, requestMeta))
        .collect(Collectors.toList());
    List<String> jobIds = new ArrayList<>(submitResults.size());
    List<String> srns = new ArrayList<>(submitResults.size());

    for (SubmitJobResult result: submitResults) {
      jobIds.add(result.getJobId());
      srns.add(result.getSrn());
    }

    //poll job for readiness
    submitService.awaitSubmitJobs(jobIds, requestMeta);

    //run enrichment
    LoadManifest reducedLoadManifest = stripRedundantFields(loadManifest);

    List<Record> records = srns.stream()
        .map(odesId -> enrichService
            .enrichRecord(odesId, reducedLoadManifest, authorizationToken, partition))
        .collect(Collectors.toList());

    //create SRN
    //store new SRN

    IngestResult ingestResult = new IngestResult();
    log.info("Request to ingest with parameters : {}, finished with the result: {}", loadManifest,
        ingestResult);
    return ingestResult;
  }

  private ProcessingReport validateManifest(LoadManifest loadManifest) {
    final JsonNode manifestDefaultSchema = getDefaultManifestSchema();

    final JsonNode jsonNodeFromManifest = getJsonNodeFromManifest(loadManifest);

    return jsonValidationService
        .validate(manifestDefaultSchema, jsonNodeFromManifest);
  }

  private JsonNode getDefaultManifestSchema() {
    try {
      return objectMapper.readTree(getClass().getClassLoader().getResource(DEFAULT_MANIFEST_SCHEMA_NAME));
    } catch (IOException e) {
      throw new IngestException("Failed to get default schema for manigests", e);
    }
  }

  private SchemaData getSchemaData(LoadManifest loadManifest) {
    String schemaSrn = getSchemaSrn(loadManifest);

    return srnMappingService.getSchemaData(schemaSrn);
  }

  private String getSchemaSrn(LoadManifest loadManifest) {
    return (String) loadManifest.getWorkProduct().getData().get(ManifestFields.RESOURCE_TYPE_ID);
  }

  private List<URL> getFileUrls(LoadManifest loadManifest) {
    try {
      final List<File> fileList = objectMapper.readValue(toJson(loadManifest.getFiles()),
              new TypeReference<List<File>>() {});
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
    try {
      return objectMapper.readTree(toJson(loadManifest));
    } catch (IOException e) {
      throw new IngestException(
          String.format("Could not convert object to JSON. Object : %s", loadManifest), e);
    }
  }

  private String transferFile(URL fileUrl, String authToken, String partition) {
    String fileName = getFileNameFromUrl(fileUrl);
    Blob blob = storageService.uploadFileToStorage(fileUrl, fileName);

    SignedUrlResult signedUrlResult = delfiIngestionClient
        .getSignedUrlForLocation(fileName, authToken, portalProperties.getAppKey(), partition);

    storageService.writeFileToSignedUrlLocation(blob, signedUrlResult.getLocationUrl());
    return signedUrlResult.getRelativeFilePath();
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


  private String getFileNameFromUrl(URL fileUrl) {
    try {
      return Paths.get(new URI(fileUrl.toString()).getPath()).getFileName().toString();
    } catch (URISyntaxException e) {
      throw new IngestException(String.format("Can not get file name from URL: %s", fileUrl), e);
    }
  }

  private LoadManifest stripRedundantFields(LoadManifest initialLoadManifest) {
    LoadManifest loadManifest = deepCopy(initialLoadManifest);
    loadManifest.setFiles(loadManifest.getFiles().stream()
        .map(file -> {
          GroupTypeProperties properties = file.getData().getGroupTypeProperties();
          properties.setFileSource(null);
          properties.setOriginalFilePath(null);
          properties.setStagingFilePath(null);
          file.getData().setGroupTypeProperties(properties);

          return file;
        }).collect(Collectors.toList()));

    return loadManifest;
  }

  private LoadManifest deepCopy(LoadManifest loadManifest) {
    try {
      return objectMapper.readValue(toJson(loadManifest),
          LoadManifest.class);
    } catch (IOException e) {
      throw new IngestException("Error processing LoadManifest json for odesId", e);
    }
  }

  private String normalizePartition(String partition) {
    return StringUtils.replace(partition,"[^a-zA-Z0-9]", "");
  }

  private <T> String toJson(T value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new IngestException("Could not convert object to JSON. Object: " + value);
    }
  }

}
