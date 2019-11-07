package com.osdu.service.delfi;

import static com.osdu.model.job.IngestJobStatus.FAILED;
import static com.osdu.service.helper.IngestionHelper.generateSrn;
import static com.osdu.service.helper.IngestionHelper.getAcl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.storage.Blob;
import com.osdu.client.DelfiIngestionClient;
import com.osdu.exception.OsduServerErrorException;
import com.osdu.model.Record;
import com.osdu.model.ResourceTypeId;
import com.osdu.model.SchemaData;
import com.osdu.model.SrnToRecord;
import com.osdu.model.delfi.DelfiRecord;
import com.osdu.model.delfi.IngestedFile;
import com.osdu.model.delfi.Legal;
import com.osdu.model.delfi.RequestMeta;
import com.osdu.model.delfi.signed.SignedFile;
import com.osdu.model.delfi.signed.SignedUrlResult;
import com.osdu.model.manifest.ManifestFields;
import com.osdu.model.manifest.ManifestFile;
import com.osdu.model.manifest.WorkProductComponent;
import com.osdu.model.property.DelfiPortalProperties;
import com.osdu.service.IngestionService;
import com.osdu.service.PortalService;
import com.osdu.service.SrnMappingService;
import com.osdu.service.StorageService;
import com.osdu.service.google.GcpSrnMappingService;
import com.osdu.service.helper.IngestionHelper;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DelfiIngestionService implements IngestionService {

  final DelfiPortalProperties portalProperties;
  final DelfiIngestionClient delfiIngestionClient;
  final SrnMappingService srnMappingService;
  final StorageService storageService;
  final PortalService portalService;
  final DelfiPortalService delfiPortalService;
  final ObjectMapper mapper;
  final GcpSrnMappingService gcpSrnMappingService;
  final IngestionHelper ingestionHelper;

  @Override
  public String createRecordForWorkProductComponent(WorkProductComponent workProductComponent, List<String> srns, RequestMeta requestMeta) {
    String resourceTypeId = workProductComponent.getResourceTypeId();
    SchemaData schemaData = gcpSrnMappingService.getSchemaData(resourceTypeId);

    // FIXME
    workProductComponent.getData().computeIfPresent(ManifestFields.GROUP_TYPE_PROPERTIES, (key, value) -> {
      ((Map<String, Object>) value).computeIfPresent(ManifestFields.FILES, (key1, value2) -> {
        ((List<String>) value2).addAll(srns);
        return value2;
      });
      return value;
    });

    DelfiRecord delfiRecord = DelfiRecord.builder()
        .kind(schemaData.getKind())
        .acl(getAcl(requestMeta.getUserGroupEmailByName()))
        .legal(getLegal(requestMeta.getLegalTags()))
        .data(workProductComponent.getData())
        .build();

    Record record = delfiPortalService.putRecord(delfiRecord, requestMeta.getAuthorizationToken(),
        requestMeta.getPartition());
    String wpcSrn = generateSrn(new ResourceTypeId(resourceTypeId));

    srnMappingService.saveSrnToRecord(SrnToRecord.builder()
        .recordId(record.getId())
        .srn(wpcSrn)
        .build());

    return wpcSrn;
  }

  @Override
  public SignedFile uploadFile(ManifestFile file, String authorizationToken, String partition) {
    URL url = ingestionHelper.createUrlFromManifestFile(file);
    SignedUrlResult result = transferFile(url, authorizationToken, partition);

    return SignedFile.builder()
        .file(file)
        .locationUrl(result.getLocationUrl())
        .relativeFilePath(result.getRelativeFilePath())
        .build();
  }

  @Override
  public SignedUrlResult transferFile(URL fileUrl, String authToken, String partition) {
    String fileName = ingestionHelper.getFileNameFromUrl(fileUrl);
    Blob blob = storageService.uploadFileToStorage(fileUrl, fileName);

    SignedUrlResult signedUrlResult = delfiIngestionClient
        .getSignedUrlForLocation(fileName, authToken, portalProperties.getAppKey(), partition);

    storageService.writeFileToSignedUrlLocation(blob, signedUrlResult.getLocationUrl());
    return signedUrlResult;
  }

  @Override
  public List<Record> failSubmittedFiles(List<IngestedFile> ingestedFiles,
      RequestMeta requestMeta) {
    List<Record> foundRecords = ingestedFiles.stream()
        .map(file -> portalService.getRecord(file.getRecordId(),
            requestMeta.getAuthorizationToken(), requestMeta.getPartition()))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    return failRecords(foundRecords, requestMeta);
  }

  @Override
  public List<Record> failRecords(List<Record> records, RequestMeta requestMeta) {
    return records.stream()
        .map(record -> {
          record.getData().put("ResourceLifecycleStatus", FAILED);
          portalService.putRecord(record, requestMeta.getAuthorizationToken(),
              requestMeta.getPartition());
          return record;
        }).collect(Collectors.toList());
  }

  private Legal getLegal(String legalTags) {
    try {
      return mapper.readValue(mapper.readTree(legalTags).get(Legal.LEGAL_ROOT).toString(),
          Legal.class);
    } catch (IOException e) {
      throw new OsduServerErrorException("Error processing load manifest", e);
    }
  }
}
