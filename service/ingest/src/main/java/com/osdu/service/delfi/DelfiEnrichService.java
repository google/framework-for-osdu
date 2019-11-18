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

import static com.osdu.service.JsonUtils.deepCopy;

import com.osdu.model.IngestHeaders;
import com.osdu.model.Record;
import com.osdu.model.delfi.IngestedFile;
import com.osdu.model.delfi.RequestMeta;
import com.osdu.model.delfi.enrich.EnrichedFile;
import com.osdu.model.type.file.OsduFile;
import com.osdu.model.type.wp.WorkProductComponent;
import com.osdu.service.EnrichService;
import com.osdu.service.PortalService;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DelfiEnrichService implements EnrichService {

  final PortalService portalService;

  @Override
  public EnrichedFile enrichRecord(IngestedFile file, RequestMeta requestMeta,
      IngestHeaders headers) {

    WorkProductComponent wpc = file.getSubmittedFile().getSignedFile().getFile().getWpc();
    WorkProductComponent reducedWpc = deepCopy(wpc, WorkProductComponent.class);

    Record record = portalService
        .getRecord(file.getRecordId(), requestMeta.getAuthorizationToken(),
            requestMeta.getPartition());

    record.getData().put("wpc", reducedWpc);
    record.getData().put("osdu", generateOsduFileRecord(file, headers));

    Record enrichedRecord = portalService.putRecord(record, requestMeta.getAuthorizationToken(),
        requestMeta.getPartition());

    return EnrichedFile.builder()
        .ingestedFile(file)
        .record(enrichedRecord)
        .build();
  }

  private OsduFile generateOsduFileRecord(IngestedFile file, IngestHeaders headers) {
    LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
    OsduFile osduFile = deepCopy(file.getSubmittedFile().getSignedFile().getFile(), OsduFile.class);
    osduFile.setResourceID(file.getSubmittedFile().getSrn());
    osduFile.setResourceHomeRegionID(headers.getResourceHomeRegionID());
    osduFile.setResourceHostRegionIDs(headers.getResourceHostRegionIDs());
    osduFile.setResourceObjectCreationDatetime(now);
    osduFile.setResourceVersionCreationDatetime(now);
    osduFile.setResourceCurationStatus("srn:reference-data/ResourceCurationStatus:CREATED:");
    osduFile.setResourceLifecycleStatus("srn:reference-data/ResourceLifecycleStatus:RECIEVED:");

    return osduFile;
  }

}
