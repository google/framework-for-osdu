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
import com.osdu.model.RequestContext;
import com.osdu.model.delfi.DelfiIngestedFile;
import com.osdu.model.delfi.enrich.EnrichedFile;
import com.osdu.model.type.file.OsduFile;
import com.osdu.model.type.wp.WorkProductComponent;
import com.osdu.service.EnrichService;
import com.osdu.service.PortalService;
import com.osdu.service.helper.IngestionHelper;
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
  public EnrichedFile enrichRecord(DelfiIngestedFile file, String srn,
      RequestContext requestContext) {

    WorkProductComponent wpc = file.getSubmittedFile().getSignedFile().getFile().getWpc();
    WorkProductComponent reducedWpc = deepCopy(wpc, WorkProductComponent.class);

    Record record = portalService
        .getRecord(file.getRecordId(), requestContext.getAuthorizationToken(),
            requestContext.getPartition());

    record.getData().put("wpc", reducedWpc);
    record.getData().put("osdu", generateOsduFileRecord(file, srn, requestContext.getHeaders()));

    Record enrichedRecord = portalService.putRecord(record, requestContext.getAuthorizationToken(),
        requestContext.getPartition());

    return EnrichedFile.builder()
        .delfiIngestedFile(file)
        .record(enrichedRecord)
        .build();
  }

  private OsduFile generateOsduFileRecord(DelfiIngestedFile file, String srn,
      IngestHeaders headers) {
    LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
    OsduFile osduFile = deepCopy(file.getSubmittedFile().getSignedFile().getFile(), OsduFile.class);

    osduFile.setResourceID(srn);
    osduFile.setResourceTypeID(IngestionHelper.prepareTypeId(osduFile.getResourceTypeID()));
    osduFile.setResourceHomeRegionID(headers.getResourceHomeRegionID());
    osduFile.setResourceHostRegionIDs(headers.getResourceHostRegionIDs());
    osduFile.setResourceObjectCreationDatetime(now);
    osduFile.setResourceVersionCreationDatetime(now);
    osduFile.setResourceCurationStatus("srn:reference-data/ResourceCurationStatus:CREATED:");
    osduFile.setResourceLifecycleStatus("srn:reference-data/ResourceLifecycleStatus:RECIEVED:");

    return osduFile;
  }

}
