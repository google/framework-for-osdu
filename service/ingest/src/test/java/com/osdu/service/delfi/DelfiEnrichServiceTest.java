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

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;

import com.osdu.ReplaceCamelCase;
import com.osdu.client.delfi.RecordDataFields;
import com.osdu.model.IngestHeaders;
import com.osdu.model.Record;
import com.osdu.model.RequestContext;
import com.osdu.model.delfi.DelfiIngestedFile;
import com.osdu.model.delfi.DelfiRecord;
import com.osdu.model.delfi.enrich.EnrichedFile;
import com.osdu.model.delfi.signed.SignedFile;
import com.osdu.model.delfi.submit.SubmittedFile;
import com.osdu.model.type.manifest.ManifestFile;
import com.osdu.model.type.manifest.ManifestWpc;
import com.osdu.service.EnrichService;
import com.osdu.service.JsonUtils;
import com.osdu.service.PortalService;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
public class DelfiEnrichServiceTest {

  private static final String AUTHORIZATION_TOKEN = "authToken";
  private static final String PARTITION = "partition";
  private static final String HOME_REGION_ID = "home_region_id";
  private static final List<String> HOST_REGION_IDS = Collections.singletonList("host_region_id");

  private static final String WPC_RESOURCE_TYPE_ID = "srn:type:work-product-component/WellLog:";
  private static final String FILE_RESOURCE_TYPE_ID = "srn:type:file/las2:";
  private static final String SRN = "srn:file/las2:2c2921dbf84f437790400bb282940fd2:1";
  private static final String RECORD_ID = "recordId";
  private static final String RECORD_KIND = "tenant:ingestion-test:wellbore:1.0.0";

  @Mock
  private PortalService portalService;

  private EnrichService enrichService;

  @BeforeEach
  public void setUp() {
    enrichService = new DelfiEnrichService(portalService);
  }

  @Test
  public void shouldEnrichFileRecord() {
    // given
    RequestContext requestContext = getRequestContext();
    DelfiIngestedFile file = getIngestedFile();
    Record record = DelfiRecord.builder()
        .id(RECORD_ID)
        .kind(RECORD_KIND)
        .version(123L)
        .data(new HashMap<>())
        .build();

    given(portalService.getRecord(RECORD_ID, AUTHORIZATION_TOKEN, PARTITION)).willReturn(record);
    given(portalService.putRecord(any(), eq(AUTHORIZATION_TOKEN), eq(PARTITION)))
        .willAnswer(this::getSaveRecordAnswer);

    // when
    EnrichedFile enrichedFile = enrichService.enrichRecord(file, SRN, requestContext);
    Record enrichedRecord = enrichedFile.getRecord();

    // then
    then(enrichedFile).isEqualToIgnoringGivenFields(EnrichedFile.builder()
        .delfiIngestedFile(file)
        .build(), "record");
    then(enrichedRecord).isEqualToIgnoringNullFields(DelfiRecord.builder()
        .id(RECORD_ID)
        .kind(RECORD_KIND)
        .version(124L)
        .build());
    then(enrichedRecord.getData().get(RecordDataFields.WPC_DATA)).isEqualTo(getWpcOsduData());
    then(enrichedRecord.getData().get(RecordDataFields.OSDU_DATA)).isEqualToIgnoringNullFields(
        getFileOsduData());

    InOrder inOrder = inOrder(portalService);
    inOrder.verify(portalService).getRecord(RECORD_ID, AUTHORIZATION_TOKEN, PARTITION);
    inOrder.verify(portalService).putRecord(any(), eq(AUTHORIZATION_TOKEN), eq(PARTITION));
    inOrder.verifyNoMoreInteractions();
  }

  private DelfiIngestedFile getIngestedFile() {
    return DelfiIngestedFile.builder()
          .recordId(RECORD_ID)
          .submittedFile(SubmittedFile.builder()
              .signedFile(SignedFile.builder()
                  .file(getManifestFile())
                  .build())
              .build())
          .build();
  }

  private RequestContext getRequestContext() {
    return RequestContext.builder()
          .authorizationToken(AUTHORIZATION_TOKEN)
          .partition(PARTITION)
          .headers(IngestHeaders.builder()
              .resourceHomeRegionID(HOME_REGION_ID)
              .resourceHostRegionIDs(HOST_REGION_IDS)
              .build())
          .build();
  }

  private ManifestFile getManifestFile() {
    ManifestWpc wpc = new ManifestWpc();
    wpc.setResourceTypeID(WPC_RESOURCE_TYPE_ID);

    ManifestFile file = new ManifestFile();
    file.setResourceTypeID(FILE_RESOURCE_TYPE_ID);
    file.setWpc(wpc);

    return file;
  }

  private Record getSaveRecordAnswer(InvocationOnMock invocation) {
    DelfiRecord record = invocation.getArgument(0);
    DelfiRecord deepCopy = JsonUtils.deepCopy(record, DelfiRecord.class);
    deepCopy.setVersion(record.getVersion() + 1);

    return deepCopy;
  }

  private Map<String, Object> getRecordData() {
    Map<String, Object> data = new HashMap<>();
    data.put(RecordDataFields.WPC_DATA, getWpcOsduData());
    data.put(RecordDataFields.OSDU_DATA, getFileOsduData());

    return data;
  }

  private Map<String, Object> getWpcOsduData() {
    Map<String, Object> data = new HashMap<>();
    data.put("ResourceTypeID", WPC_RESOURCE_TYPE_ID);

    return data;
  }

  private Map<String, Object> getFileOsduData() {
    Map<String, Object> data = new HashMap<>();
    data.put("ResourceID", SRN);
    data.put("ResourceTypeID", FILE_RESOURCE_TYPE_ID);
    data.put("ResourceHomeRegionID", HOME_REGION_ID);
    data.put("ResourceHostRegionIDs", HOST_REGION_IDS);
    data.put("ResourceCurationStatus", "srn:reference-data/ResourceCurationStatus:CREATED:");
    data.put("ResourceLifecycleStatus", "srn:reference-data/ResourceLifecycleStatus:RECIEVED:");

    return data;
  }

}