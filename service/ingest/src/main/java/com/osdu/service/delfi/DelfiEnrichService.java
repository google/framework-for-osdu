package com.osdu.service.delfi;

import static com.osdu.model.delfi.status.MasterJobStatus.COMPLETED;
import static com.osdu.request.OsduHeader.RESOURCE_HOME_REGION_ID;
import static com.osdu.request.OsduHeader.RESOURCE_HOST_REGION_IDS;
import static com.osdu.request.OsduHeader.extractHeaderByName;
import static com.osdu.service.JsonUtils.toJson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.osdu.exception.IngestException;
import com.osdu.model.Record;
import com.osdu.model.delfi.RequestMeta;
import com.osdu.model.delfi.enrich.EnrichedFile;
import com.osdu.model.delfi.submit.SubmittedFile;
import com.osdu.model.manifest.WorkProductComponent;
import com.osdu.service.EnrichService;
import com.osdu.service.PortalService;
import java.io.IOException;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DelfiEnrichService implements EnrichService {

  final ObjectMapper objectMapper;
  final PortalService portalService;

  @Override
  public EnrichedFile enrichRecord(SubmittedFile file, RequestMeta requestMeta,
      MessageHeaders headers) {

    WorkProductComponent wpc = file.getSignedFile().getFile().getWpc();
    Record record = portalService.getRecord("recordId", requestMeta.getAuthorizationToken(),
        requestMeta.getPartition());

    WorkProductComponent reducedWpc = stripRedundantFields(deepCopy(wpc));
    record.getData().putAll(reducedWpc.getData());

    record.getData().putAll(defineAdditionalProperties(headers));

    Record savedRecord = portalService.putRecord(record, requestMeta.getAuthorizationToken(),
        requestMeta.getPartition());

    return EnrichedFile.builder()
        .submittedFile(file)
        .record(savedRecord)
        .build();
  }

  private Map<String, Object> defineAdditionalProperties(MessageHeaders headers) {
    Map<String, Object> properties = new HashMap<>();
    properties.put(RESOURCE_HOME_REGION_ID, extractHeaderByName(headers, RESOURCE_HOME_REGION_ID));
    properties
        .put(RESOURCE_HOST_REGION_IDS, extractHeaderByName(headers, RESOURCE_HOST_REGION_IDS));
    Clock clock = Clock.systemUTC();
    // TODO fix logic with versions
    properties.put("ResourceObjectCreationDateTime", clock.millis());
    properties.put("ResourceVersionCreationDateTime", clock.millis());

    properties.put("ResourceCurationStatus", "CREATED");
    properties.put("ResourceLifecycleStatus", COMPLETED);
    properties.put("ResourceSecurityClassification", "RESTRICTED");

    return properties;
  }

  private WorkProductComponent stripRedundantFields(WorkProductComponent wpc) {
    // TODO Remove redundant values from work product component
    return wpc;
  }

  private WorkProductComponent deepCopy(WorkProductComponent wpc) {
    try {
      return objectMapper.readValue(toJson(wpc),
          WorkProductComponent.class);
    } catch (IOException e) {
      throw new IngestException("Error processing WorkProductComponent json", e);
    }
  }

}
