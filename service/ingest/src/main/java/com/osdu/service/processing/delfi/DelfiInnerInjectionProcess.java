package com.osdu.service.processing.delfi;

import static com.osdu.model.job.IngestJobStatus.FAILED;
import static com.osdu.request.OsduHeader.extractHeaderByName;
import static com.osdu.service.JsonUtils.getJsonNode;

import com.google.cloud.storage.Blob;
import com.osdu.client.DelfiEntitlementsClient;
import com.osdu.client.DelfiIngestionClient;
import com.osdu.exception.IngestException;
import com.osdu.model.Record;
import com.osdu.model.ResourceTypeId;
import com.osdu.model.SchemaData;
import com.osdu.model.SrnToRecord;
import com.osdu.model.delfi.IngestedFile;
import com.osdu.model.delfi.RequestMeta;
import com.osdu.model.delfi.enrich.EnrichedFile;
import com.osdu.model.delfi.entitlement.Group;
import com.osdu.model.delfi.signed.SignedFile;
import com.osdu.model.delfi.signed.SignedUrlResult;
import com.osdu.model.delfi.status.JobsPullingResult;
import com.osdu.model.delfi.submit.SubmitJobResult;
import com.osdu.model.delfi.submit.SubmittedFile;
import com.osdu.model.job.IngestJob;
import com.osdu.model.job.IngestJobStatus;
import com.osdu.model.manifest.LoadManifest;
import com.osdu.model.manifest.ManifestFile;
import com.osdu.model.manifest.WorkProductComponent;
import com.osdu.model.property.DelfiPortalProperties;
import com.osdu.request.OsduHeader;
import com.osdu.service.EnrichService;
import com.osdu.service.JobStatusService;
import com.osdu.service.PortalService;
import com.osdu.service.SrnMappingService;
import com.osdu.service.StorageService;
import com.osdu.service.SubmitService;
import com.osdu.service.processing.InnerInjectionProcess;
import com.osdu.service.validation.JsonValidationService;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

  private static final Pattern PARTITION_PATTERN = Pattern.compile("[^a-zA-Z0-9]+");

  final DelfiPortalProperties portalProperties;

  final DelfiIngestionClient delfiIngestionClient;
  final DelfiEntitlementsClient delfiEntitlementsClient;

  final SrnMappingService srnMappingService;
  final StorageService storageService;
  final EnrichService enrichService;
  final SubmitService submitService;
  final JobStatusService jobStatusService;
  final PortalService portalService;
  final JsonValidationService jsonValidationService;

  @Override
  @Async
  public void process(String innerJobId, LoadManifest loadManifest, MessageHeaders headers) {
    log.info("Start the internal async injection process. JobId: {}, loadManifest: {}, headers: {}",
        innerJobId, loadManifest, headers);
    String authorizationToken = extractHeaderByName(headers, OsduHeader.AUTHORIZATION);
    String partition = normalizePartition(extractHeaderByName(headers, OsduHeader.PARTITION));
    String legalTags = extractHeaderByName(headers, OsduHeader.LEGAL_TAGS);

    IngestJobStatus ingestJobStatus[] = {IngestJobStatus.COMPLETE};
    List<String> srns = new ArrayList<>();

    Map<String, String> groupEmailByName = delfiEntitlementsClient
        .getUserGroups(authorizationToken, portalProperties.getAppKey(), partition)
        .getGroups().stream()
        .collect(Collectors.toMap(Group::getName, Group::getEmail));
    log.info("Fetched the user groups aka permissions. JobId: {}, user groups: {}", innerJobId,
        groupEmailByName);

    getWorkProductComponents(loadManifest)
        .forEach(wpc -> {
          SchemaData schemaData = srnMappingService.getSchemaData(wpc.getResourceTypeId());

          RequestMeta requestMeta = RequestMeta.builder()
              .authorizationToken(authorizationToken)
              .partition(partition)
              .legalTags(legalTags)
              .schemaData(schemaData)
              .userGroupEmailByName(groupEmailByName)
              .resourceTypeId(new ResourceTypeId(wpc.getResourceTypeId()))
              .build();

          List<SubmittedFile> submittedFiles = wpc.getFiles().stream()
              .map(file -> uploadFile(file, authorizationToken, partition))
              .map(file -> submitFile(file, requestMeta))
              .collect(Collectors.toList());

          Map<String, SubmittedFile> jobIdToFile = submittedFiles.stream()
              .collect(Collectors.toMap(SubmittedFile::getJobId, Function.identity()));

          List<String> jobIds = new ArrayList<>(jobIdToFile.keySet());

          //poll job for readiness
          log.info("Waited for all submitted jobs to be finished. JobId: {}", innerJobId);
          JobsPullingResult jobsPullingResult = submitService.awaitSubmitJobs(jobIds, requestMeta);

          // fail records if at least one submit job fail
          log.debug("Pulling ingestion job result: {}", jobsPullingResult);
          if (!jobsPullingResult.getFailedJobs().isEmpty()) {
            ingestJobStatus[0] = FAILED;
            failSubmittedFiles(submittedFiles, requestMeta);
            return;
          }

          // get record ID from ingestion job metadata
          List<IngestedFile> ingestedFiles = submitService.getIngestionResult(jobsPullingResult,
              jobIdToFile, requestMeta);

          //run enrichment
          List<EnrichedFile> enrichedFiles = ingestedFiles.stream()
              .map(file -> enrichService.enrichRecord(file, requestMeta, headers))
              .collect(Collectors.toList());
          log.info("Finished the enrichment. JobId: {}", innerJobId);

          // post validation
          enrichedFiles.stream()
              .map(file -> jsonValidationService
                  .validate(schemaData.getSchema(), getJsonNode(file.getRecord())))
              .filter(result -> !result.isSuccess())
              .peek(result -> log.warn("Post submit record validation fail - " + result.toString()))
              .findFirst()
              .ifPresent(result -> {
                ingestJobStatus[0] = FAILED;
                List<Record> enrichedRecords = enrichedFiles.stream()
                    .map(EnrichedFile::getRecord)
                    .collect(Collectors.toList());
                failRecords(enrichedRecords, requestMeta);
              });

          enrichedFiles.forEach(file -> {
            srnMappingService.saveSrnToRecord(SrnToRecord.builder()
                .recordId(file.getRecord().getId())
                .srn(file.getIngestedFile().getSubmittedFile().getSrn())
                .build());
            srns.add(file.getIngestedFile().getSubmittedFile().getSrn());
          });
        });

    jobStatusService.save(IngestJob.builder()
        .id(innerJobId)
        .status(ingestJobStatus[0])
        .srns(srns)
        .build());
    log.info("Finished the internal async injection process. JobId: {}", innerJobId);
  }

  private String normalizePartition(String partition) {
    return RegExUtils.replaceAll(partition, PARTITION_PATTERN, "");
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
      return new URL(file.getData().getGroupTypeProperties().getOriginalFilePath());
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

  private List<Record> failSubmittedFiles(List<SubmittedFile> submittedFiles,
      RequestMeta requestMeta) {
    // TODO fix get record logic
    List<Record> foundRecords = submittedFiles.stream()
        .map(file -> portalService.getRecord(file.getSrn(),
            requestMeta.getAuthorizationToken(), requestMeta.getPartition()))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    return failRecords(foundRecords, requestMeta);
  }

  private List<Record> failRecords(List<Record> records, RequestMeta requestMeta) {
    return records.stream()
        .map(record -> {
          record.getData().put("ResourceLifecycleStatus", FAILED);
          // TODO fix get record logic
          portalService.putRecord(record, requestMeta.getAuthorizationToken(),
              requestMeta.getPartition());
          return record;
        }).collect(Collectors.toList());
  }

}
