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

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.osdu.exception.OsduException;
import com.osdu.model.osdu.delivery.delfi.ProcessingResult;
import com.osdu.model.osdu.delivery.dto.DeliveryResponse;
import com.osdu.model.osdu.delivery.input.InputPayload;
import com.osdu.request.OsduHeader;
import com.osdu.service.AuthenticationService;
import com.osdu.service.processing.DataProcessingJob;
import com.osdu.service.processing.ResultDataConverter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.messaging.MessageHeaders;

@RunWith(MockitoJUnitRunner.class)
public class DelfiDeliveryServiceTest {

  private static final String AUTH = "auth";
  private static final String PARTITION = "partition";
  private static final String SRN_1 = "srn1";
  private static final String SRN_2 = "srn2";
  private static final String REGION_ID = "regionId";

  @Mock
  ResultDataConverter resultDataConverter;

  @Mock
  AuthenticationService authenticationService;

  @Mock
  DataProcessingJob dataProcessingJob;

  @Mock
  CompletableFuture<ProcessingResult> processingResultCompletableFutureOne;
  @Mock
  CompletableFuture<ProcessingResult> processingResultCompletableFutureTwo;

  @InjectMocks
  private DelfiDeliveryService delfiDeliveryService;

  @Test
  public void shouldGetRecord() throws ExecutionException, InterruptedException {

    // given
    ProcessingResult result1 = new ProcessingResult();
    ProcessingResult result2 = new ProcessingResult();

    when(dataProcessingJob.process(eq(SRN_1), eq(AUTH), eq(PARTITION)))
        .thenReturn(processingResultCompletableFutureOne);
    when(dataProcessingJob.process(eq(SRN_2), eq(AUTH), eq(PARTITION)))
        .thenReturn(processingResultCompletableFutureTwo);
    when(processingResultCompletableFutureOne.get()).thenReturn(result1);
    when(processingResultCompletableFutureTwo.get()).thenReturn(result2);

    DeliveryResponse deliveryResponse = new DeliveryResponse();
    when(resultDataConverter.convertProcessingResults(eq(Arrays.asList(result1, result2))))
        .thenReturn(deliveryResponse);

    InputPayload inputPayload = new InputPayload(Arrays.asList(SRN_1, SRN_2), REGION_ID);

    Map<String, Object> map = new HashMap<>();
    map.put(OsduHeader.AUTHORIZATION, AUTH);
    map.put(OsduHeader.PARTITION, PARTITION);
    MessageHeaders headers = new MessageHeaders(map);

    // when
    DeliveryResponse response = delfiDeliveryService.getResources(inputPayload, headers);

    // then
    assertEquals(response, deliveryResponse);
  }

  @Test
  public void shouldFailGetRecordWithErrorInJob() throws ExecutionException, InterruptedException {

    // given
    when(dataProcessingJob.process(any(), any(), any()))
        .thenReturn(processingResultCompletableFutureOne);
    when(processingResultCompletableFutureOne.get())
        .thenThrow(new ExecutionException(new RuntimeException()));

    InputPayload inputPayload = new InputPayload(Collections.singletonList(SRN_1), REGION_ID);

    MessageHeaders headers = new MessageHeaders(new HashMap<>());

    // when
    Throwable thrown = catchThrowable(
        () -> delfiDeliveryService.getResources(inputPayload, headers));

    // then
    Assertions.assertThat(thrown)
        .isInstanceOf(OsduException.class)
        .hasMessageContaining("Error execution srn -");
  }
}
