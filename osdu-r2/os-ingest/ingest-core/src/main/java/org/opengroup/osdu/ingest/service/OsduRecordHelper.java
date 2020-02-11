/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.ingest.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.opengroup.osdu.ingest.model.type.file.FileData;
import org.opengroup.osdu.ingest.model.type.file.FileGroupTypeProperties;
import org.opengroup.osdu.ingest.model.type.file.OsduFile;
import org.opengroup.osdu.ingest.model.type.resource.ResourceCurationStatus;
import org.opengroup.osdu.ingest.model.type.resource.ResourceLifecycleStatus;
import org.springframework.stereotype.Service;

@Service
public class OsduRecordHelper {

  public OsduFile populateOsduRecord(String fileLocation) {

    String defaultResourceType = "srn:type:file/las2:1";
    String defaultResourceHomeRegionID = "srn:reference-data/OSDURegion:test:1";
    List<String> defaultResourceHostRegionIDs = Collections
        .singletonList("srn:reference-data/OSDURegion:test:1");
    String resourceSecurityClassification =
        "srn:reference-data/ResourceSecurityClassification:RESTRICTED:";

    LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

    FileGroupTypeProperties fileGroupTypeProperties = FileGroupTypeProperties
        .builder()
        .fileSource(fileLocation)
        .build();
    FileData fileData = FileData.builder()
        .groupTypeProperties(fileGroupTypeProperties)
        .build();

    return OsduFile.builder()
        .resourceID(generateFileSrn())
        .resourceTypeID(defaultResourceType)
        .data(fileData)
        .resourceHomeRegionID(defaultResourceHomeRegionID)
        .resourceHostRegionIDs(defaultResourceHostRegionIDs)
        .resourceObjectCreationDatetime(now)
        .resourceVersionCreationDatetime(now)
        .resourceCurationStatus(ResourceCurationStatus.CREATED.toResourceId())
        .resourceLifecycleStatus(ResourceLifecycleStatus.RECEIVED.toResourceId())
        .resourceSecurityClassification(resourceSecurityClassification)
        .build();
  }

  private String generateFileSrn() {
    String defaultFileType = "file/las2";
    String uuid = UUID.randomUUID().toString().replace("-", "");
    return String.format("srn:%s:%s:1", defaultFileType, uuid);
  }
}
