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

package com.osdu.service.google;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;

import com.osdu.ReplaceCamelCase;
import com.osdu.exception.IngestJobNotFoundException;
import com.osdu.mapper.IngestJobMapper;
import com.osdu.model.job.IngestJob;
import com.osdu.model.job.IngestJobStatus;
import com.osdu.model.job.IngestJobStatusDto;
import com.osdu.repository.IngestJobRepository;
import com.osdu.request.OsduHeader;
import com.osdu.service.AuthenticationService;
import com.osdu.service.JobStatusService;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.MessageHeaders;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
public class GcpJobStatusServiceTest {

  private static final String AUTHORIZATION_TOKEN = "authToken";
  private static final String PARTITION = "partition";
  private static final String INGEST_JOB_ID = "2b0c651b-040b-4f06-8b77-5f6c8bc9b8a1";
  private static final List<String> SRNS = Arrays.asList(
      "srn:file/las2:f39220f248f14757922c57511bd4f8dd:",
      "srn:work-product-component/WellLog:35b34404eb974031ab8ac56230c38238:1");

  @Mock
  private AuthenticationService authenticationService;
  @Mock
  private IngestJobRepository ingestJobRepository;
  @Spy
  private IngestJobMapper ingestJobMapper = Mappers.getMapper(IngestJobMapper.class);
  @Captor
  ArgumentCaptor<IngestJob> ingestJobCaptor;
  @Captor
  ArgumentCaptor<Map<String, Object>> fieldsCaptor;

  private JobStatusService jobStatusService;

  @BeforeEach
  public void setUp() {
    jobStatusService = new GcpJobStatusService(authenticationService, ingestJobRepository,
        ingestJobMapper);
  }

  @Test
  public void shouldGetStatusOfIngestJob() {
    // given
    MessageHeaders headers = getMessageHeaders();

    given(ingestJobRepository.findById(INGEST_JOB_ID)).willReturn(Optional.of(getIngestJob()));

    // when
    IngestJobStatusDto jobStatusDto = jobStatusService.getStatus(INGEST_JOB_ID, headers);

    // then
    then(jobStatusDto).isEqualTo(IngestJobStatusDto.builder()
        .id(INGEST_JOB_ID)
        .status(IngestJobStatus.COMPLETE)
        .srns(SRNS)
        .build());

    InOrder inOrder = inOrder(authenticationService, ingestJobRepository, ingestJobMapper);
    inOrder.verify(authenticationService).checkAuthentication(AUTHORIZATION_TOKEN, PARTITION);
    inOrder.verify(ingestJobRepository).findById(INGEST_JOB_ID);
    inOrder.verify(ingestJobMapper).toStatusDto(any());
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void shouldThrowNotFoundExceptionWhenUnableToGetIngestJobStatus() {
    // given
    MessageHeaders headers = getMessageHeaders();

    given(ingestJobRepository.findById(INGEST_JOB_ID)).willReturn(Optional.empty());

    // when
    Throwable thrown = catchThrowable(() -> jobStatusService.getStatus(INGEST_JOB_ID, headers));

    // then
    then(thrown)
        .isInstanceOf(IngestJobNotFoundException.class)
        .hasMessage("Not ingest job found by id = " + INGEST_JOB_ID);

    InOrder inOrder = inOrder(authenticationService, ingestJobRepository, ingestJobMapper);
    inOrder.verify(authenticationService).checkAuthentication(AUTHORIZATION_TOKEN, PARTITION);
    inOrder.verify(ingestJobRepository).findById(INGEST_JOB_ID);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void shouldGetIngestJob() {
    // given
    IngestJob ingestJob = getIngestJob();
    given(ingestJobRepository.findById(INGEST_JOB_ID)).willReturn(Optional.of(ingestJob));

    // when
    IngestJob actualIngestJob = jobStatusService.get(INGEST_JOB_ID);

    // then
    then(actualIngestJob).isEqualTo(ingestJob);

    InOrder inOrder = inOrder(authenticationService, ingestJobRepository, ingestJobMapper);
    inOrder.verify(ingestJobRepository).findById(INGEST_JOB_ID);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void shouldThrowNotFoundExceptionWhenUnableToFindIngestJob() {
    // given
    given(ingestJobRepository.findById(INGEST_JOB_ID)).willReturn(Optional.empty());

    // when
    Throwable thrown = catchThrowable(() -> jobStatusService.get(INGEST_JOB_ID));

    // then
    then(thrown)
        .isInstanceOf(IngestJobNotFoundException.class)
        .hasMessage("Not ingest job found by id = " + INGEST_JOB_ID);

    InOrder inOrder = inOrder(authenticationService, ingestJobRepository, ingestJobMapper);
    inOrder.verify(ingestJobRepository).findById(INGEST_JOB_ID);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void shouldInitInjectJob() {
    // when
    String injectJobId = jobStatusService.initInjectJob();

    // then
    then(injectJobId).is(new Condition<>(this::isValidUuid, "Valid UUID"));

    InOrder inOrder = inOrder(authenticationService, ingestJobRepository, ingestJobMapper);
    inOrder.verify(ingestJobRepository).save(ingestJobCaptor.capture());
    inOrder.verifyNoMoreInteractions();

    IngestJob ingestJob = ingestJobCaptor.getValue();
    then(ingestJob.getId()).isNotBlank();
    then(ingestJob.getStatus()).isEqualTo(IngestJobStatus.CREATED);
  }

  @Test
  public void shouldUpdateJobStatus() {
    // when
    jobStatusService.updateJobStatus(INGEST_JOB_ID, IngestJobStatus.RUNNING);

    // then
    InOrder inOrder = inOrder(authenticationService, ingestJobRepository, ingestJobMapper);
    inOrder.verify(ingestJobRepository).updateFields(eq(INGEST_JOB_ID), fieldsCaptor.capture());
    inOrder.verifyNoMoreInteractions();

    then(fieldsCaptor.getValue()).containsOnly(entry("status", "RUNNING"));
  }

  @Test
  public void shouldSaveIngestJob() {
    // given
    IngestJob ingestJob = IngestJob.builder()
        .build();

    // when
    jobStatusService.save(ingestJob);

    // then
    InOrder inOrder = inOrder(authenticationService, ingestJobRepository, ingestJobMapper);
    inOrder.verify(ingestJobRepository).save(ingestJobCaptor.capture());
    inOrder.verifyNoMoreInteractions();

    then(ingestJobCaptor.getValue()).isEqualTo(ingestJob);
  }

  private MessageHeaders getMessageHeaders() {
    HashMap<String, Object> headers = new HashMap<>();
    headers.put(OsduHeader.AUTHORIZATION, AUTHORIZATION_TOKEN);
    headers.put(OsduHeader.PARTITION, PARTITION);

    headers.values().removeIf(Objects::isNull);

    return new MessageHeaders(headers);
  }

  private IngestJob getIngestJob() {
    return IngestJob.builder()
        .id(INGEST_JOB_ID)
        .status(IngestJobStatus.COMPLETE)
        .srns(SRNS)
        .created(Date.from(Instant.now()))
        .build();
  }

  private boolean isValidUuid(String value) {
    try {
      UUID uuid = UUID.fromString(value);
      return uuid != null;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

}