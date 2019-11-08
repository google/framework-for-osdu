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

package com.osdu.service.processing.delfi;

import static com.osdu.service.processing.delfi.DelfiDataProcessingJob.LOCATION_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.osdu.model.Record;
import com.osdu.model.SrnToRecord;
import com.osdu.model.delfi.DelfiFile;
import com.osdu.model.osdu.delivery.delfi.ProcessingResult;
import com.osdu.model.osdu.delivery.delfi.ProcessingResultStatus;
import com.osdu.service.PortalService;
import com.osdu.service.SrnMappingService;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DelfiDataProcessingJobTest {

  @Mock
  private SrnMappingService srnMappingService;
  @Mock
  private PortalService portalService;

  private static final String AUTHORIZATION_TOKEN = "authToken";
  private static final String PARTITION = "partition";
  private static final String SRN = "srn";
  private static final String SIGNED_URL = "signedUrl";
  private static final String RECORD_ID_1 = "recordId1";

  private DelfiDataProcessingJob dataProcessingJob;

  @Before
  public void init() {
    dataProcessingJob = new DelfiDataProcessingJob(srnMappingService, portalService);
  }

  @Test
  public void testNoLocation() throws Exception {
    // given
    SrnToRecord srnToRecord = SrnToRecord.builder().recordId(RECORD_ID_1).srn(SRN).build();
    when(srnMappingService.getSrnToRecord(eq(SRN))).thenReturn(srnToRecord);

    Record record = new Record();
    Map<String, Object> data = new HashMap<>();
    data.put("one", "test");

    Map<String, Object> details = new HashMap<>();
    details.put("two", "test");

    record.setAdditionalProperties(details);
    record.setData(data);

    when(portalService.getRecord(eq(RECORD_ID_1), eq(AUTHORIZATION_TOKEN), eq(PARTITION)))
        .thenReturn(record);

    // when
    CompletableFuture<ProcessingResult> future = dataProcessingJob
        .process(SRN, AUTHORIZATION_TOKEN, PARTITION);
    ProcessingResult result = future.get();

    // then
    assertThat(result.getProcessingResultStatus()).isEqualTo(ProcessingResultStatus.DATA);
    assertThat(result.getFileLocation()).isNull();
    assertThat(result.getSrn()).isEqualTo(SRN);
    assertThat(result.getData()).isEqualTo(record);
  }

  @Test
  public void testWithFileLocation() throws Exception {
    // given
    SrnToRecord srnToRecord = SrnToRecord.builder().recordId(RECORD_ID_1).srn(SRN).build();
    when(srnMappingService.getSrnToRecord(eq(SRN))).thenReturn(srnToRecord);

    Record record = new Record();
    Map<String, Object> data = new HashMap<>();
    data.put(LOCATION_KEY, "test location");
    Map<String, Object> details = new HashMap<>();
    details.put("two", "test");
    record.setAdditionalProperties(details);
    record.setData(data);
    when(portalService.getRecord(eq(RECORD_ID_1), eq(AUTHORIZATION_TOKEN), eq(PARTITION)))
        .thenReturn(record);

    DelfiFile delfiFile = new DelfiFile();
    delfiFile.setSignedUrl(SIGNED_URL);
    when(portalService.getFile(eq("test location"), eq(AUTHORIZATION_TOKEN), eq(PARTITION)))
        .thenReturn(delfiFile);

    // when
    CompletableFuture<ProcessingResult> future = dataProcessingJob
        .process(SRN, AUTHORIZATION_TOKEN, PARTITION);
    ProcessingResult result = future.get();

    // then
    assertThat(result.getProcessingResultStatus()).isEqualTo(ProcessingResultStatus.FILE);
    assertThat(result.getFileLocation()).isEqualTo(SIGNED_URL);
    assertThat(result.getSrn()).isEqualTo(SRN);
  }

  @Test
  public void testNoMapping() throws Exception {
    // given
    when(srnMappingService.getSrnToRecord(eq(SRN))).thenReturn(null);

    // when
    CompletableFuture<ProcessingResult> future = dataProcessingJob
        .process(SRN, AUTHORIZATION_TOKEN, PARTITION);
    ProcessingResult result = future.get();

    // then
    assertThat(result.getProcessingResultStatus()).isEqualTo(ProcessingResultStatus.NO_MAPPING);
    assertThat(result.getFileLocation()).isNull();
    assertThat(result.getSrn()).isEqualTo(SRN);
    assertThat(result.getData()).isNull();
  }
}
