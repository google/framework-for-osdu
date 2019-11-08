/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.osdu.service.delfi;

import static com.osdu.model.delfi.status.MasterJobStatus.COMPLETED;
import static com.osdu.request.OsduHeader.RESOURCE_HOME_REGION_ID;
import static com.osdu.request.OsduHeader.RESOURCE_HOST_REGION_IDS;
import static com.osdu.service.JsonUtils.toJson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.osdu.exception.IngestException;
import com.osdu.model.IngestHeaders;
import com.osdu.model.Record;
import com.osdu.model.delfi.IngestedFile;
import com.osdu.model.delfi.RequestMeta;
import com.osdu.model.delfi.enrich.EnrichedFile;
import com.osdu.model.manifest.WorkProductComponent;
import com.osdu.service.EnrichService;
import com.osdu.service.PortalService;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DelfiEnrichService implements EnrichService {

  final ObjectMapper objectMapper;
  final PortalService portalService;

  @Override
  public EnrichedFile enrichRecord(IngestedFile file, RequestMeta requestMeta,
      IngestHeaders headers) {

    WorkProductComponent wpc = file.getSubmittedFile().getSignedFile().getFile().getWpc();
    WorkProductComponent reducedWpc = stripRedundantFields(deepCopy(wpc));

    Record record = portalService
        .getRecord(file.getRecordId(), requestMeta.getAuthorizationToken(),
            requestMeta.getPartition());

    record.getData().put("Data", reducedWpc.getData());
    record.getData().putAll(defineAdditionalProperties(headers));

    Record enrichedRecord = portalService.putRecord(record, requestMeta.getAuthorizationToken(),
        requestMeta.getPartition());

    return EnrichedFile.builder()
        .ingestedFile(file)
        .record(enrichedRecord)
        .build();
  }

  private Map<String, Object> defineAdditionalProperties(IngestHeaders headers) {
    Map<String, Object> properties = new HashMap<>();
    properties.put(RESOURCE_HOME_REGION_ID, headers.getHomeRegionID());
    properties.put(RESOURCE_HOST_REGION_IDS, headers.getHostRegionIDs());
    LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
    // TODO fix logic with versions
    properties.put("ResourceObjectCreationDateTime", now);
    properties.put("ResourceVersionCreationDateTime", now);

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
      return objectMapper.readValue(toJson(wpc), WorkProductComponent.class);
    } catch (IOException e) {
      throw new IngestException("Error processing WorkProductComponent json", e);
    }
  }

}
