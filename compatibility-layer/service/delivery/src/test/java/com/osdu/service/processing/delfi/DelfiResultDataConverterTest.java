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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import com.osdu.model.Record;
import com.osdu.model.osdu.delivery.delfi.ProcessingResult;
import com.osdu.model.osdu.delivery.delfi.ProcessingResultStatus;
import com.osdu.model.osdu.delivery.dto.DeliveryResponse;
import com.osdu.model.osdu.delivery.dto.ResponseFileLocation;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DelfiResultDataConverterTest {

  private static final String SRN_1 = "srn_1";
  private static final String ONE = "one";
  private static final String TWO = "two";
  private static final String TEST = "test";
  private static final String SRN_2 = "srn_2";
  private static final String SRN_3 = "srn_3";

  @InjectMocks
  private DelfiResultDataConverter resultDataConverter;

  @Test
  public void shouldConvertDataRecordResult() {

    // given
    Map<String, Object> data = new HashMap<>();
    data.put(ONE, TEST);
    data.put(TWO, TEST);
    Record record = new Record();
    record.setData(data);

    ProcessingResult dataResult = createProcessingResult(ProcessingResultStatus.DATA, null, data,
        SRN_1);

    // when
    DeliveryResponse response = resultDataConverter
        .convertProcessingResults(Collections.singletonList(dataResult));

    // then
    assertThat(response.getUnprocessedSrns()).isEmpty();
    assertThat(response.getResult()).hasSize(1);
    assertEquals(response.getResult().get(0).getData(), data);
    assertThat(response.getResult().get(0).getFileLocation()).isNull();
    assertThat(response.getResult().get(0).getSrn()).isEqualTo(SRN_1);
  }

  @Test
  public void shouldConvertDataRecordFileRecordAndNoMappingResults() {

    // given
    Map<String, Object> data = new HashMap<>();
    data.put(ONE, TEST);
    data.put(TWO, TEST);
    Record record = new Record();
    record.setData(data);
    ProcessingResult dataResult = createProcessingResult(ProcessingResultStatus.DATA, null, data,
        SRN_1);

    Record fileRecord = new Record();
    fileRecord.setAdditionalProperties(data);
    ProcessingResult fileResult = createProcessingResult(ProcessingResultStatus.FILE,
        "http://url.com", data, SRN_2);

    ProcessingResult noMappingResult = createProcessingResult(ProcessingResultStatus.NO_MAPPING,
        null, null, SRN_3);

    List<ProcessingResult> results = Arrays.asList(dataResult, fileResult, noMappingResult);

    // when
    DeliveryResponse response = resultDataConverter.convertProcessingResults(results);

    // then
    assertThat(response.getResult()).hasSize(2);
    assertEquals(response.getResult().get(0).getData(), data);
    assertThat(response.getResult().get(0).getFileLocation()).isNull();
    assertThat(response.getResult().get(0).getSrn()).isEqualTo(SRN_1);

    assertEquals(response.getResult().get(1).getData(), data);
    assertEquals(response.getResult().get(1).getFileLocation(),
        new ResponseFileLocation("http://url.com"));
    assertThat(response.getResult().get(1).getSrn()).isEqualTo(SRN_2);

    assertThat(response.getUnprocessedSrns()).hasSize(1);
    assertThat(response.getUnprocessedSrns().get(0)).isEqualTo(SRN_3);
  }

  private ProcessingResult createProcessingResult(ProcessingResultStatus status,
      String fileLocation, Map<String, Object> data, String srn) {
    ProcessingResult processingResult = new ProcessingResult();
    processingResult.setProcessingResultStatus(status);
    processingResult.setFileLocation(fileLocation);
    processingResult.setData(data);
    processingResult.setSrn(srn);

    return processingResult;
  }
}
