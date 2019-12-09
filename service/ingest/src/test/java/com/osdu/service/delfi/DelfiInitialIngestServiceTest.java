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

import static com.osdu.service.JsonUtils.toObject;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;

import com.osdu.ReplaceCamelCase;
import com.osdu.exception.IngestException;
import com.osdu.exception.OsduBadRequestException;
import com.osdu.exception.OsduUnauthorizedException;
import com.osdu.mapper.IngestHeadersMapper;
import com.osdu.messaging.IngestPubSubGateway;
import com.osdu.model.IngestHeaders;
import com.osdu.model.IngestResult;
import com.osdu.model.delfi.submit.LegalTagsObject;
import com.osdu.model.job.IngestMessage;
import com.osdu.model.type.manifest.LoadManifest;
import com.osdu.request.OsduHeader;
import com.osdu.service.AuthenticationService;
import com.osdu.service.InitialIngestService;
import com.osdu.service.JobStatusService;
import com.osdu.service.validation.LoadManifestValidationService;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
public class DelfiInitialIngestServiceTest {

  private static final String AUTHORIZATION_TOKEN = "authToken";
  private static final String PARTITION = "partition";
  private static final String LEGAL_TAGS = "{\"legal\":{\"legaltags\":[\"tenant-public-usa-dataset-1\"],\"otherRelevantDataCountries\":[\"US\"]}}";
  private static final String HOME_REGION_ID = "home_region_id";
  private static final String HOST_REGION_IDS_RAW = "[\"host_region_id_1\", \"host_region_id_2\"]";
  private static final List<String> HOST_REGION_IDS = Arrays.asList("host_region_id_1", "host_region_id_2");

  private static final String INGEST_JOB_ID = "ingest-job-id";

  @Mock
  private JobStatusService jobStatusService;
  @Mock
  private LoadManifestValidationService loadManifestValidationService;
  @Mock
  private IngestPubSubGateway ingestGateway;
  @Mock
  private AuthenticationService authenticationService;
  @Spy
  private IngestHeadersMapper ingestHeadersMapper = Mappers.getMapper(IngestHeadersMapper.class);
  @Captor
  ArgumentCaptor<IngestMessage> ingestMessageCaptor;

  private InitialIngestService initialIngestService;

  @BeforeEach
  public void setUp() {
    initialIngestService = new DelfiInitialIngestService(jobStatusService,
        loadManifestValidationService, ingestGateway, authenticationService, ingestHeadersMapper);
  }

  @Test
  public void shouldInitiateIngestAndSendMessageToGateway() {
    // given
    LoadManifest loadManifest = LoadManifest.builder().build();
    MessageHeaders headers = getMessageHeaders();

    given(jobStatusService.initInjectJob()).willReturn(INGEST_JOB_ID);

    // when
    IngestResult ingestResult = initialIngestService.ingestManifest(loadManifest, headers);

    // then
    then(ingestResult).isEqualTo(IngestResult.builder()
        .jobId(INGEST_JOB_ID)
        .build());

    InOrder inOrder = inOrder(jobStatusService, loadManifestValidationService,
        ingestGateway, authenticationService, ingestHeadersMapper);
    inOrder.verify(ingestHeadersMapper).toIngestHeaders(headers);
    inOrder.verify(authenticationService).checkAuthentication(AUTHORIZATION_TOKEN, PARTITION);
    inOrder.verify(loadManifestValidationService).validateManifest(loadManifest);
    inOrder.verify(jobStatusService).initInjectJob();
    inOrder.verify(ingestGateway).sendIngestToPubSub(ingestMessageCaptor.capture());
    inOrder.verifyNoMoreInteractions();

    then(ingestMessageCaptor.getValue()).isEqualTo(getIngestMessage(loadManifest));
  }

  @Test
  public void shouldThrowUnauthorizedExceptionWhenCheckAuthorizationIsFailed() {
    // given
    LoadManifest loadManifest = LoadManifest.builder().build();
    MessageHeaders headers = getMessageHeaders();

    willThrow(OsduUnauthorizedException.class)
        .given(authenticationService).checkAuthentication(AUTHORIZATION_TOKEN, PARTITION);

    // when
    Throwable thrown = catchThrowable(() -> initialIngestService.ingestManifest(loadManifest, headers));

    // then
    then(thrown).isInstanceOf(OsduUnauthorizedException.class);

    InOrder inOrder = inOrder(jobStatusService, loadManifestValidationService,
        ingestGateway, authenticationService, ingestHeadersMapper);
    inOrder.verify(ingestHeadersMapper).toIngestHeaders(headers);
    inOrder.verify(authenticationService).checkAuthentication(AUTHORIZATION_TOKEN, PARTITION);
    inOrder.verify(loadManifestValidationService, never()).validateManifest(any());
    inOrder.verify(jobStatusService, never()).initInjectJob();
    inOrder.verify(ingestGateway, never()).sendIngestToPubSub(any());
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void shouldThrowIngestExceptionWhenManifestIsInvalid() {
    // given
    LoadManifest loadManifest = LoadManifest.builder().build();
    MessageHeaders headers = getMessageHeaders();

    willThrow(IngestException.class).given(loadManifestValidationService).validateManifest(loadManifest);

    // when
    Throwable thrown = catchThrowable(() -> initialIngestService.ingestManifest(loadManifest, headers));

    // then
    then(thrown).isInstanceOf(IngestException.class);

    InOrder inOrder = inOrder(jobStatusService, loadManifestValidationService,
        ingestGateway, authenticationService, ingestHeadersMapper);
    inOrder.verify(ingestHeadersMapper).toIngestHeaders(headers);
    inOrder.verify(authenticationService).checkAuthentication(AUTHORIZATION_TOKEN, PARTITION);
    inOrder.verify(loadManifestValidationService).validateManifest(loadManifest);
    inOrder.verify(jobStatusService, never()).initInjectJob();
    inOrder.verify(ingestGateway, never()).sendIngestToPubSub(any());
    inOrder.verifyNoMoreInteractions();
  }

  @ParameterizedTest(name = "#{index}: Exception should have message \"{1}\"")
  @MethodSource("missingHeaderProvider")
  public void shouldThrowBadRequestExceptionWhenSomeHeaderIsMissing(MessageHeaders headers,
      String expectedErrorMessage) {
    // given
    LoadManifest loadManifest = LoadManifest.builder().build();

    // when
    Throwable thrown = catchThrowable(() -> initialIngestService.ingestManifest(loadManifest, headers));

    // then
    then(thrown)
        .isInstanceOf(OsduBadRequestException.class)
        .hasMessage(expectedErrorMessage);

    InOrder inOrder = inOrder(jobStatusService, loadManifestValidationService,
        ingestGateway, authenticationService, ingestHeadersMapper);
    inOrder.verify(ingestHeadersMapper, never()).toIngestHeaders(any());
    inOrder.verify(authenticationService, never()).checkAuthentication(any(), any());
    inOrder.verify(loadManifestValidationService, never()).validateManifest(any());
    inOrder.verify(jobStatusService, never()).initInjectJob();
    inOrder.verify(ingestGateway, never()).sendIngestToPubSub(any());
    inOrder.verifyNoMoreInteractions();
  }

  private static Stream<Arguments> missingHeaderProvider() {
    return Stream.of(
        Arguments.arguments(getMessageHeaders(null, PARTITION, LEGAL_TAGS,
            HOME_REGION_ID, HOST_REGION_IDS_RAW), "Missing authorization token"),
        Arguments.arguments(getMessageHeaders(AUTHORIZATION_TOKEN, null, LEGAL_TAGS,
            HOME_REGION_ID, HOST_REGION_IDS_RAW), "Missing partition"),
        Arguments.arguments(getMessageHeaders(AUTHORIZATION_TOKEN, PARTITION, null,
            HOME_REGION_ID, HOST_REGION_IDS_RAW), "Missing \"legal-tags\" header"),
        Arguments.arguments(getMessageHeaders(AUTHORIZATION_TOKEN, PARTITION, LEGAL_TAGS,
            null, HOST_REGION_IDS_RAW), "Missing \"resource-home-region-id\" header"),
        Arguments.arguments(getMessageHeaders(AUTHORIZATION_TOKEN, PARTITION, LEGAL_TAGS,
            HOME_REGION_ID, null), "Missing \"resource-host-region-ids\" header")
    );
  }

  private static MessageHeaders getMessageHeaders(String authorization, String partition,
      String legalTags, String homeRegionId, String hostRegionIds) {
    HashMap<String, Object> headers = new HashMap<>();
    headers.put(OsduHeader.AUTHORIZATION, authorization);
    headers.put(OsduHeader.PARTITION, partition);
    headers.put(OsduHeader.LEGAL_TAGS, legalTags);
    headers.put(OsduHeader.RESOURCE_HOME_REGION_ID, homeRegionId);
    headers.put(OsduHeader.RESOURCE_HOST_REGION_IDS, hostRegionIds);

    headers.values().removeIf(Objects::isNull);

    return new MessageHeaders(headers);
  }

  private static MessageHeaders getMessageHeaders() {
    return getMessageHeaders(AUTHORIZATION_TOKEN, PARTITION, LEGAL_TAGS,
        HOME_REGION_ID, HOST_REGION_IDS_RAW);
  }

  private IngestMessage getIngestMessage(LoadManifest loadManifest) {
    return IngestMessage.builder()
        .loadManifest(loadManifest)
        .ingestJobId(INGEST_JOB_ID)
        .headers(IngestHeaders.builder()
            .authorizationToken(AUTHORIZATION_TOKEN)
            .partition(PARTITION)
            .legalTags(LEGAL_TAGS)
            .legalTagsObject(toObject(LEGAL_TAGS, LegalTagsObject.class))
            .resourceHomeRegionID(HOME_REGION_ID)
            .resourceHostRegionIDs(HOST_REGION_IDS)
            .build())
        .build();
  }

}