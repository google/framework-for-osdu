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

import static com.osdu.service.processing.delfi.DelfiDataProcessingJob.BUCKET_URL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.osdu.client.DelfiEntitlementsClient;
import com.osdu.model.Record;
import com.osdu.model.SrnToRecord;
import com.osdu.model.delfi.DelfiFile;
import com.osdu.model.delfi.entitlement.Group;
import com.osdu.model.delfi.entitlement.UserGroups;
import com.osdu.model.osdu.delivery.dto.DeliveryResponse;
import com.osdu.model.osdu.delivery.dto.ResponseFileLocation;
import com.osdu.model.osdu.delivery.dto.ResponseItem;
import com.osdu.model.osdu.delivery.input.InputPayload;
import com.osdu.model.property.DelfiPortalProperties;
import com.osdu.request.OsduHeader;
import com.osdu.service.PortalService;
import com.osdu.service.SrnMappingService;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.MessageHeaders;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class DelfiDeliveryServiceIntegrationTest {

  private static final String APP_KEY = "appKey";
  private static final String DES_ID = "desId";
  private static final String EMAIL = "member@emaul.com";
  private static final String AUTH = "auth";
  private static final String PARTITION = "partition";
  private static final String SRN_1 = "srn1";
  private static final String SRN_2 = "srn2";
  private static final String SRN_3 = "srn3";
  private static final String RECORD_1 = "record1";
  private static final String RECORD_2 = "record2";
  private static final String TEST = "test";
  private static final String URL = "url";
  private static final String SIGNED_URL = "signedUrl";
  private static final String REGION_ID = "regionId";
  private static final String OSDU = "osdu";

  @Autowired
  DelfiDeliveryService delfiDeliveryService;

  @MockBean
  DelfiEntitlementsClient delfiEntitlementsClient;

  @MockBean
  SrnMappingService srnMappingService;

  @MockBean
  DelfiPortalProperties portalProperties;

  @MockBean
  PortalService portalService;

  @Test
  public void shouldGetRecord() {

    when(portalProperties.getAppKey()).thenReturn(APP_KEY);

    UserGroups userGroups = new UserGroups();
    userGroups.setDesId(DES_ID);
    userGroups.setMemberEmail(EMAIL);
    userGroups.setGroups(Collections.singletonList(new Group()));
    when(delfiEntitlementsClient.getUserGroups(eq(AUTH), eq(APP_KEY), eq(PARTITION)))
        .thenReturn(userGroups);

    when(srnMappingService.getSrnToRecord(eq(SRN_1)))
        .thenReturn(SrnToRecord.builder().srn(SRN_1).recordId(
            RECORD_1).build());
    when(srnMappingService.getSrnToRecord(eq(SRN_2)))
        .thenReturn(SrnToRecord.builder().srn(SRN_2).recordId(
            RECORD_2).build());

    Record dataRecord = new Record();
    Map<String, Object> osduData = new HashMap<>();
    osduData.put(TEST, TEST);
    Map<String, Object> data = new HashMap<>();
    data.put(OSDU, osduData);
    dataRecord.setData(data);
    when(portalService.getRecord(eq(RECORD_1), eq(AUTH), eq(PARTITION))).thenReturn(dataRecord);

    Record fileRecord = new Record();
    Map<String, Object> fileOsduData = new HashMap<>();
    fileOsduData.put(TEST, TEST);
    Map<String, Object> fileData = new HashMap<>();
    fileData.put(OSDU, fileOsduData);
    fileData.put(BUCKET_URL, URL);
    fileRecord.setData(fileData);
    DelfiFile delfiFile = new DelfiFile();
    delfiFile.setSignedUrl(SIGNED_URL);
    when(portalService.getFile(eq(URL), eq(AUTH), eq(PARTITION))).thenReturn(delfiFile);
    when(portalService.getRecord(eq(RECORD_2), eq(AUTH), eq(PARTITION))).thenReturn(fileRecord);

    InputPayload inputPayload = new InputPayload(Arrays.asList(SRN_1, SRN_2, SRN_3), REGION_ID);

    Map<String, Object> map = new HashMap<>();
    map.put(OsduHeader.AUTHORIZATION, AUTH);
    map.put(OsduHeader.PARTITION, PARTITION);
    MessageHeaders headers = new MessageHeaders(map);

    DeliveryResponse resources = delfiDeliveryService.getResources(inputPayload, headers);
    System.out.println(resources);

    assertThat(resources.getUnprocessedSrns()).containsExactly(SRN_3);
    assertThat(resources.getResult()).hasSize(2);

    ResponseItem dataResponse = resources.getResult().get(0);
    assertEquals(SRN_1, dataResponse.getSrn());
    assertEquals(osduData, dataResponse.getData());
    assertNull(dataResponse.getFileLocation());

    ResponseItem fileResponse = resources.getResult().get(1);
    assertEquals(SRN_2, fileResponse.getSrn());
    assertEquals(fileOsduData, fileResponse.getData());
    assertEquals(fileResponse.getFileLocation(), new ResponseFileLocation(SIGNED_URL));
  }
}
