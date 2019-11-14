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


import static com.osdu.request.OsduHeader.RESOURCE_HOME_REGION_ID;
import static com.osdu.request.OsduHeader.RESOURCE_HOST_REGION_IDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.osdu.model.IngestHeaders;
import com.osdu.model.Record;
import com.osdu.model.delfi.IngestedFile;
import com.osdu.model.delfi.RequestMeta;
import com.osdu.model.delfi.enrich.EnrichedFile;
import com.osdu.model.delfi.signed.SignedFile;
import com.osdu.model.delfi.submit.SubmittedFile;
import com.osdu.model.type.manifest.ManifestFile;
import com.osdu.model.type.wp.WorkProductComponent;
import com.osdu.service.PortalService;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DelfiEnrichServiceTest {

  private static final String AUTHORIZATION_TOKEN = "authToken";
  private static final String PARTITION = "partition";
  private static final String TEST_KEY = "test";
  private static final String TEST_VALUE = "test-value";
  private static final String RECORD_ID = "recordId";
  private static final String HOME_REGION_ID = "home_region_id";
  private static final String HOST_REGION_ID = "host_region_id";

  private ObjectMapper objectMapper = new ObjectMapper();

  @Mock
  private PortalService portalService;

  private DelfiEnrichService delfiEnrichService;

  @Before
  public void initialize() {
    delfiEnrichService = new DelfiEnrichService(objectMapper, portalService);
  }

  @Test
  @Ignore
  public void shouldEnrichRecord() {

    // given
    HashMap<String, Object> data = new HashMap<>();
    data.put(TEST_KEY, TEST_VALUE);
    WorkProductComponent wpc = null/*WorkProductComponent.builder()
        .data(data).build()*/;
    ManifestFile manifestFile = null/*ManifestFile.builder().wpc(wpc).build()*/;
    SignedFile signedFile = SignedFile.builder().file(manifestFile).build();
    SubmittedFile file = SubmittedFile.builder().signedFile(signedFile).build();
    IngestedFile ingestedFile = IngestedFile.builder().submittedFile(file).build();

    Record record = new Record();
    record.setData(new HashMap<>());

    when(portalService.getRecord(eq(RECORD_ID), eq(AUTHORIZATION_TOKEN), eq(PARTITION)))
        .thenReturn(record);
    when(portalService.putRecord(eq(record), eq(AUTHORIZATION_TOKEN), eq(PARTITION)))
        .thenAnswer(i -> i.getArguments()[0]);

    RequestMeta meta = RequestMeta.builder().authorizationToken(AUTHORIZATION_TOKEN)
        .partition(PARTITION).build();

    IngestHeaders ingestHeaders = IngestHeaders.builder()
        .homeRegionID(HOME_REGION_ID)
        .hostRegionIDs(HOST_REGION_ID)
        .build();

    // when
    EnrichedFile enrichedFile = delfiEnrichService.enrichRecord(ingestedFile, meta, ingestHeaders);

    // then
    Map<String, Object> enrichedData = enrichedFile.getRecord().getData();
    assertThat(enrichedData.get(RESOURCE_HOME_REGION_ID)).isEqualTo(HOME_REGION_ID);
    assertThat(enrichedData.get(RESOURCE_HOST_REGION_IDS)).isEqualTo(HOST_REGION_ID);
    assertThat(enrichedData.get(RESOURCE_HOST_REGION_IDS)).isEqualTo(HOST_REGION_ID);
    assertThat(enrichedData.get(TEST_KEY)).isEqualTo(TEST_VALUE);
  }
}
