package com.osdu.service.delfi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.osdu.exception.IngestException;
import com.osdu.model.Record;
import com.osdu.model.delfi.IngestRecord;
import com.osdu.model.delfi.RequestMeta;
import com.osdu.model.delfi.submit.SubmittedFile;
import com.osdu.model.manifest.GroupTypeProperties;
import com.osdu.model.manifest.LoadManifest;
import com.osdu.service.EnrichService;
import com.osdu.service.PortalService;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DelfiEnrichService implements EnrichService {

  final ObjectMapper objectMapper;
  final PortalService portalService;

  @Override
  public Record enrichRecord(String odesId, LoadManifest loadManifest, String authorizationToken,
      String partition) {

    LoadManifest reducedLoadManifest = stripRedundantFields(loadManifest);

    final Record record = portalService.getRecord(odesId, authorizationToken, partition);

    populateRecordWithManifest(reducedLoadManifest, record);

    return portalService.putRecord(odesId, record, authorizationToken, partition);
  }

  @Override
  public List<Record> enrichRecords(List<SubmittedFile> ingestRecords, RequestMeta requestMeta) {
    // TODO: rework
    List<Record> records = ingestRecords.stream()
        .map(file -> {
          Record record = portalService.getRecord("", requestMeta.getAuthorizationToken(),
              requestMeta.getPartition());
          return IngestRecord.builder()
              .submittedFile(file)
              .record(record)
              .build();
        })
        .map(record -> {
          // populate wpc: record.getSubmittedFile().getSignedFile().getFile().getWpc()
          return record.getRecord();
        })
        .collect(Collectors.toList());

    // validate

    return records;
  }

  private Record populateRecordWithManifest(LoadManifest loadManifest, Record record) {
    record.getDetails().put("WorkProduct", loadManifest.getWorkProduct());
    record.getDetails().put("WorkProductComponents", loadManifest.getWorkProductComponents());
    record.getDetails().put("Files", loadManifest.getFiles());
    record.getDetails().putAll(loadManifest.getAdditionalProperties());

    return record;
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

  private <T> String toJson(T value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new IngestException("Could not convert object to JSON. Object: " + value);
    }
  }

}
