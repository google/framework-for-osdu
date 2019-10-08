package com.osdu.service.processing.delfi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.storage.Blob;
import com.osdu.client.DelfiEntitlementsClient;
import com.osdu.client.DelfiIngestionClient;
import com.osdu.exception.IngestException;
import com.osdu.model.SchemaData;
import com.osdu.model.delfi.RequestMeta;
import com.osdu.model.delfi.entitlement.Group;
import com.osdu.model.delfi.signed.SignedFile;
import com.osdu.model.delfi.signed.SignedUrlResult;
import com.osdu.model.delfi.submit.SubmitJobResult;
import com.osdu.model.delfi.submit.SubmittedFile;
import com.osdu.model.job.IngestJobStatus;
import com.osdu.model.manifest.LoadManifest;
import com.osdu.model.manifest.ManifestFields;
import com.osdu.model.manifest.ManifestFile;
import com.osdu.model.manifest.WorkProductComponent;
import com.osdu.model.property.DelfiPortalProperties;
import com.osdu.request.OsduHeader;
import com.osdu.service.EnrichService;
import com.osdu.service.JobStatusService;
import com.osdu.service.SrnMappingService;
import com.osdu.service.StorageService;
import com.osdu.service.SubmitService;
import com.osdu.service.processing.InnerInjectionProcess;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RegExUtils;
import org.springframework.messaging.MessageHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DelfiInnerInjectionProcess implements InnerInjectionProcess {

  static final Pattern PARTITION_PATTERN = Pattern.compile("[^a-zA-Z0-9]+");
  static final Pattern DOCUMENT_ID_PATTERN = Pattern.compile("[^/]+");

  final DelfiPortalProperties portalProperties;

  final ObjectMapper objectMapper;

  final DelfiIngestionClient delfiIngestionClient;
  final DelfiEntitlementsClient delfiEntitlementsClient;

  final SrnMappingService srnMappingService;
  final StorageService storageService;
  final EnrichService enrichService;
  final SubmitService submitService;
  final JobStatusService jobStatusService;

  @Override
  @Async
  public void process(String innerJobId, LoadManifest loadManifest, MessageHeaders headers) {
    log.info("Start the internal async injection process. JobId: {}, loadManifest: {}, headers: {}",
        innerJobId, loadManifest, headers);
    String authorizationToken = extractHeaderByName(headers, OsduHeader.AUTHORIZATION);
    String partition = normalizeString(extractHeaderByName(headers, OsduHeader.PARTITION),
        PARTITION_PATTERN);
    String legalTags = extractHeaderByName(headers, OsduHeader.LEGAL_TAGS);

    Map<String, String> groupEmailByName = delfiEntitlementsClient
        .getUserGroups(authorizationToken, portalProperties.getAppKey(), partition)
        .getGroups().stream()
        .collect(Collectors.toMap(Group::getName, Group::getEmail));
    log.info("Fetched the user groups aka permissions. JobId: {}, user groups: {}", innerJobId,
        groupEmailByName);

    getWorkProductComponents(loadManifest)
        .forEach(wpc -> {
          SchemaData schemaData = getSchemaData(wpc);

          RequestMeta requestMeta = RequestMeta.builder()
              .authorizationToken(authorizationToken)
              .partition(partition)
              .legalTags(legalTags)
              .schemaData(schemaData)
              .userGroupEmailByName(groupEmailByName)
              .build();

          List<SubmittedFile> submittedFiles = wpc.getFiles().stream()
              .map(file -> uploadFile(file, authorizationToken, partition))
              .map(file -> submitFile(file, requestMeta))
              .collect(Collectors.toList());

          List<String> jobIds = submittedFiles.stream()
              .map(SubmittedFile::getJobId)
              .collect(Collectors.toList());

          //poll job for readiness
          submitService.awaitSubmitJobs(jobIds, requestMeta);
          log.info("Waited for all submitted jobs to be finished. JobId: {}", innerJobId);

          //run enrichment
          enrichService.enrichRecords(submittedFiles, requestMeta);
          log.info("Finished the enrichment. JobId: {}", innerJobId);
        });

    jobStatusService.updateJobStatus(innerJobId, IngestJobStatus.COMPLETE);
    log.info("Finished the internal async injection process. JobId: {}", innerJobId);
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

  private String normalizeString(String str, Pattern pattern) {
    return RegExUtils.replaceAll(str, pattern, "");
  }

  private List<WorkProductComponent> getWorkProductComponents(LoadManifest loadManifest) {
    Map<String, ManifestFile> fileById = loadManifest.getFiles().stream()
        .collect(Collectors.toMap(ManifestFile::getAssociativeId, Function.identity()));
    return loadManifest.getWorkProductComponents().stream()
        .map(wpc -> wpc.toBuilder()
            .files(wpc.getFileAssociativeIds().stream()
                .map(fileById::get)
                .map(file -> file.toBuilder()
                    .wpc(wpc)
                    .build())
                .collect(Collectors.toList()))
            .build())
        .collect(Collectors.toList());
  }

  private SchemaData getSchemaData(WorkProductComponent wpc) {
    String wpcTypeId = normalizeString(getWpcTypeId(wpc), DOCUMENT_ID_PATTERN);

    return srnMappingService.getSchemaData(wpcTypeId);
  }

  private String getWpcTypeId(WorkProductComponent wpc) {
    return (String) wpc.getData().get(ManifestFields.RESOURCE_TYPE_ID);
  }

  private SignedFile uploadFile(ManifestFile file, String authorizationToken, String partition) {
    URL url = createUrlFromManifestFile(file);
    SignedUrlResult result = transferFile(url, authorizationToken, partition);

    return SignedFile.builder()
        .file(file)
        .locationUrl(result.getLocationUrl())
        .relativeFilePath(result.getRelativeFilePath())
        .build();
  }

  private URL createUrlFromManifestFile(ManifestFile file) {
    try {
      return new URL(file.getData().getGroupTypeProperties().getStagingFilePath());
    } catch (MalformedURLException e) {
      throw new IngestException(
          String.format("Could not create URL from staging link : %s",
              file.getData().getGroupTypeProperties().getStagingFilePath()),
          e);
    }
  }

  private SignedUrlResult transferFile(URL fileUrl, String authToken, String partition) {
    String fileName = getFileNameFromUrl(fileUrl);
    Blob blob = storageService.uploadFileToStorage(fileUrl, fileName);

    SignedUrlResult signedUrlResult = delfiIngestionClient
        .getSignedUrlForLocation(fileName, authToken, portalProperties.getAppKey(), partition);

    storageService.writeFileToSignedUrlLocation(blob, signedUrlResult.getLocationUrl());
    return signedUrlResult;
  }

  private String getFileNameFromUrl(URL fileUrl) {
    try {
      return Paths.get(new URI(fileUrl.toString()).getPath()).getFileName().toString();
    } catch (URISyntaxException e) {
      throw new IngestException(String.format("Can not get file name from URL: %s", fileUrl), e);
    }
  }

  private SubmittedFile submitFile(SignedFile file, RequestMeta requestMeta) {
    SubmitJobResult result = submitService
        .submitFile(file.getRelativeFilePath(), requestMeta);

    return SubmittedFile.builder()
        .signedFile(file)
        .srn(result.getSrn())
        .jobId(result.getJobId())
        .build();
  }

}
