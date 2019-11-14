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

package com.osdu.integration;

import static com.osdu.service.delfi.DelfiDeliveryService.AUTHORIZATION_HEADER_KEY;
import static com.osdu.service.delfi.DelfiDeliveryService.PARTITION_HEADER_KEY;
import static com.osdu.service.processing.delfi.DelfiDataProcessingJob.BUCKET_URL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.osdu.model.SrnToRecord;
import com.osdu.model.delfi.DelfiFile;
import com.osdu.model.delfi.DelfiRecord;
import com.osdu.model.osdu.delivery.dto.DeliveryResponse;
import com.osdu.model.osdu.delivery.dto.ResponseItem;
import com.osdu.model.osdu.delivery.input.InputPayload;
import com.osdu.service.AuthenticationService;
import com.osdu.service.PortalService;
import com.osdu.service.SrnMappingService;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class DeliveryFlowIntegrationTest {

  private static final String TARGET_REGION = "targetRegion";
  private static final String SIGNED_URL = "http://signed-url";
  private static final String FILE_LOCATION = "fileLocation";
  private static final String AUTHENTICATION = "authentication";
  private static final String PARTITION = "partition";
  private static final String NO_MAPPING_EXAMPLE = "no-mapping-example";
  private static final String NO_LOCATION_EXAMPLE = "no-location-example";
  private static final String LOCATION_EXAMPLE = "location-example";
  private static final String SRN_1 = "srn1";
  private static final String SRN_2 = "srn2";
  private static final String RECORD_ID_1 = "recordId1";
  private static final String RECORD_ID_2 = "recordId2";

  @MockBean
  private SrnMappingService srnMappingService;
  @MockBean
  private AuthenticationService authenticationService;
  @MockBean
  private PortalService portalService;
  @Autowired
  private MockMvc mockMvc;
  private ObjectMapper mapper = new ObjectMapper();

  @Test
  @Ignore
  public void shouldDeliverRecords() throws Exception {

    // given
    when(authenticationService.getUserGroups(eq(AUTHENTICATION), eq(PARTITION)))
        .thenReturn(null);

    // no mapping record
    when(srnMappingService.getSchemaData(any())).thenReturn(null);

    // no file record
    DelfiRecord recordNoLocation = new DelfiRecord() {
    };
    Map<String, Object> data = new HashMap<>();
    data.put("test", "test");
    recordNoLocation.setData(data);
    SrnToRecord srnToRecord1 = SrnToRecord.builder().recordId(RECORD_ID_1).srn(SRN_1).build();
    when(srnMappingService.getSrnToRecord(eq(NO_LOCATION_EXAMPLE))).thenReturn(srnToRecord1);
    when(portalService.getRecord(eq(RECORD_ID_1), eq(AUTHENTICATION), eq(PARTITION)))
        .thenReturn(recordNoLocation);

    // file record
    DelfiRecord recordWithLocation = new DelfiRecord() {
    };
    Map<String, Object> dataLocation = new HashMap<>();
    dataLocation.put("one", "test");
    dataLocation.put(BUCKET_URL, FILE_LOCATION);
    recordWithLocation.setData(dataLocation);
    SrnToRecord srnToRecord2 = SrnToRecord.builder().recordId(RECORD_ID_2).srn(SRN_2).build();
    when(srnMappingService.getSrnToRecord(eq(LOCATION_EXAMPLE))).thenReturn(srnToRecord2);
    when(portalService.getRecord(eq(RECORD_ID_2), eq(AUTHENTICATION), eq(PARTITION)))
        .thenReturn(recordWithLocation);
    DelfiFile fileRecord = new DelfiFile();
    fileRecord.setSignedUrl(SIGNED_URL);
    when(portalService.getFile(eq(FILE_LOCATION), eq(AUTHENTICATION), eq(PARTITION)))
        .thenReturn(fileRecord);

    List<String> srns = Arrays.asList(NO_LOCATION_EXAMPLE, NO_MAPPING_EXAMPLE, LOCATION_EXAMPLE);
    InputPayload payload = new InputPayload(srns, TARGET_REGION);
    HttpHeaders headers = new HttpHeaders();
    headers.add(AUTHORIZATION_HEADER_KEY, AUTHENTICATION);
    headers.add(PARTITION_HEADER_KEY, PARTITION);

    // when
    ResponseEntity responseEntity = (ResponseEntity) mockMvc
        .perform(MockMvcRequestBuilders.post("/")
            .headers(headers)
            .content(mapper.writeValueAsString(payload)))
        .andExpect(status().isOk())
        .andReturn().getAsyncResult();

    // then
    DeliveryResponse response = (DeliveryResponse) responseEntity.getBody();

    assertNotNull(response);
    assertEquals(Collections.singletonList(NO_MAPPING_EXAMPLE), response.getUnprocessedSrns());
    assertThat(response.getResult()).hasSize(2);
    List<ResponseItem> items = response.getResult();

    assertThat(items.get(0).getSrn()).isEqualTo(NO_LOCATION_EXAMPLE);
    assertThat(items.get(0).getFileLocation()).isNull();
    Map<String, Object> noLocationData = ((DelfiRecord) items.get(0).getData()).getData();
    assertThat(noLocationData.get("test")).isEqualTo("test");

    assertThat(items.get(1).getSrn()).isEqualTo(LOCATION_EXAMPLE);
    assertThat(items.get(1).getFileLocation()).isEqualTo(SIGNED_URL);
  }

}
