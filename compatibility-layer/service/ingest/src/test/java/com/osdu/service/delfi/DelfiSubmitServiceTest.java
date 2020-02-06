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

import static com.osdu.model.delfi.status.MasterJobStatus.COMPLETED;
import static com.osdu.model.delfi.status.MasterJobStatus.FAILED;
import static com.osdu.model.delfi.status.MasterJobStatus.RUNNING;
import static java.lang.String.format;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.timeout;

import com.osdu.ReplaceCamelCase;
import com.osdu.client.DelfiIngestionClient;
import com.osdu.model.RequestContext;
import com.osdu.model.delfi.DelfiFile;
import com.osdu.model.delfi.DelfiIngestedFile;
import com.osdu.model.delfi.status.JobInfo;
import com.osdu.model.delfi.status.JobPollingResult;
import com.osdu.model.delfi.status.JobStatusResponse;
import com.osdu.model.delfi.status.MasterJobStatus;
import com.osdu.model.delfi.status.Summary;
import com.osdu.model.delfi.submit.FileInput;
import com.osdu.model.delfi.submit.SubmitFileContext;
import com.osdu.model.delfi.submit.SubmitFileObject;
import com.osdu.model.delfi.submit.SubmitFileResult;
import com.osdu.model.delfi.submit.SubmitJobResult;
import com.osdu.model.delfi.submit.SubmittedFile;
import com.osdu.model.property.DelfiPortalProperties;
import com.osdu.model.property.SubmitProperties;
import com.osdu.service.SubmitService;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.AdditionalAnswers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
public class DelfiSubmitServiceTest {

  private static final String APP_KEY = "appKey";
  private static final String AUTHORIZATION_TOKEN = "authToken";
  private static final String PARTITION = "partition";

  private static final String JOB_ID_1 = "jobId-1";
  private static final String JOB_ID_2 = "jobId-2";
  private static final String JOB_ID_3 = "jobId-3";

  private static final String STORAGE_HREF = "http://storage.host.com";
  private static final String GCS_PROTOCOL = "gs:/";
  private static final String SUCCESS_METADATA_JSON_PATH = "successrecords/success-metadata.json";

  private static final String RECORD_KIND = "tenant:ingestion-test:wellbore:1.0.0";
  private static final String WPC_RESOURCE_TYPE_ID = "srn:type:work-product-component/WellLog:version1";
  private static final String FILE_RESOURCE_TYPE_ID = "srn:type:file/las2:version1";
  private static final String DELFI_RECORD_ID_1 = "recordId-1";
  private static final String OWNER_EMAIL_1 = "data.default.owners@tenant.host.com";
  private static final String VIEWER_EMAIL_1 = "data.default.viewers@tenant.host.com";
  private static final String DATA_DEFAULT_OWNERS = "data.default.owners";
  private static final String DATA_DEFAULT_VIEWERS = "data.default.viewers";
  private static final String LAS_INGESTOR = "[{\"LASIngestor\":{\"createRawWellRecord\":true}}]";

  @Mock
  private RestTemplate restTemplate;
  @Mock
  private DelfiIngestionClient delfiIngestionClient;
  @Mock
  private DelfiPortalService portalService;

  private DelfiPortalProperties portalProperties = DelfiPortalProperties.builder()
      .appKey(APP_KEY)
      .build();
  private SubmitProperties submitProperties = SubmitProperties.builder()
      .pollingInterval(10)
      .pollingCycles(3)
      .build();

  private SubmitService submitService;

  @BeforeEach
  public void setUp() {
    submitService = new DelfiSubmitService(portalProperties, submitProperties, restTemplate,
        delfiIngestionClient, portalService);
  }

  @ParameterizedTest(name = "#{index}: Job (jobId = {0}) should be in status {2}")
  @MethodSource("awaitJobProvider")
  public void shouldPollAndAwaitingStatusOfTheSubmittedIngestionJob(String jobId,
      List<MasterJobStatus> jobStatuses, MasterJobStatus expectedStatus) {
    // given
    RequestContext requestContext = getRequestContext();

    List<JobStatusResponse> jobStatusResponses = jobStatuses.stream()
        .map(status -> getJobStatusResponse(jobId, status))
        .collect(Collectors.toList());

    given(delfiIngestionClient.getJobStatus(jobId, AUTHORIZATION_TOKEN, APP_KEY, PARTITION))
        .willAnswer(AdditionalAnswers.returnsElementsOf(jobStatusResponses));

    // when
    JobPollingResult jobPollingResult = submitService.awaitSubmitJob(jobId, requestContext);

    // then
    then(jobPollingResult).isEqualToIgnoringGivenFields(JobPollingResult.builder()
        .runningJob(jobId)
        .status(expectedStatus)
        .build(), "job");

    InOrder inOrder = Mockito.inOrder(restTemplate, delfiIngestionClient, portalService);
    inOrder.verify(delfiIngestionClient, timeout(7 * 10).times(jobStatuses.size()))
        .getJobStatus(jobId, AUTHORIZATION_TOKEN, APP_KEY, PARTITION);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void shouldSubmitFileToIngestIntoDatalakeUsingLasIngestor() {
    // given
    String fileRelativePath = "/some-landing-zone/some-user/uuid/file-name-1.las";
    RequestContext requestContext = getRequestContext();
    SubmitFileContext fileContext = SubmitFileContext.builder()
        .relativeFilePath(fileRelativePath)
        .kind(RECORD_KIND)
        .wpcResourceTypeId(WPC_RESOURCE_TYPE_ID)
        .fileResourceTypeId(FILE_RESOURCE_TYPE_ID)
        .build();
    SubmitFileObject fileObject = getSubmitFileObject(fileContext, LAS_INGESTOR);

    given(delfiIngestionClient.submitFile(AUTHORIZATION_TOKEN, APP_KEY, PARTITION, PARTITION, fileObject))
        .willReturn(SubmitFileResult.builder().jobId(JOB_ID_1).build());

    // when
    SubmitJobResult submitJobResult = submitService.submitFile(fileContext, requestContext);

    // then
    then(submitJobResult).isEqualTo(SubmitJobResult.builder()
        .jobId(JOB_ID_1)
        .build());

    InOrder inOrder = Mockito.inOrder(restTemplate, delfiIngestionClient, portalService);
    inOrder.verify(delfiIngestionClient)
        .submitFile(AUTHORIZATION_TOKEN, APP_KEY, PARTITION, PARTITION, fileObject);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void shouldSubmitFileToIngestIntoDatalakeUsingDefaultIngestor() {
    // given
    String fileRelativePath = "/some-landing-zone/some-user/uuid/file-name-2.csv";
    RequestContext requestContext = getRequestContext();
    SubmitFileContext fileContext = SubmitFileContext.builder()
        .relativeFilePath(fileRelativePath)
        .kind("tenant:ingestion-test:wellbore-traj:1.0.0")
        .wpcResourceTypeId("srn:type:work-product-component/WellboreTrajectory:version1")
        .fileResourceTypeId("srn:type:file/csv:version1")
        .build();
    SubmitFileObject fileObject = getSubmitFileObject(fileContext, null);

    given(delfiIngestionClient.submitFile(AUTHORIZATION_TOKEN, APP_KEY, PARTITION, PARTITION, fileObject))
        .willReturn(SubmitFileResult.builder().jobId(JOB_ID_2).build());

    // when
    SubmitJobResult submitJobResult = submitService.submitFile(fileContext, requestContext);

    // then
    then(submitJobResult).isEqualTo(SubmitJobResult.builder()
        .jobId(JOB_ID_2)
        .build());

    InOrder inOrder = Mockito.inOrder(restTemplate, delfiIngestionClient, portalService);
    inOrder.verify(delfiIngestionClient)
        .submitFile(AUTHORIZATION_TOKEN, APP_KEY, PARTITION, PARTITION, fileObject);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void shouldFetchRecordIdOfIngestedFile() {
    // given
    String outputLocation = "/some-ingestion-persistent-zone/some-user/uuid/output";
    String fileRelativePath = outputLocation + SUCCESS_METADATA_JSON_PATH;
    String fileUri = GCS_PROTOCOL + fileRelativePath;
    String fileSignedUrl = STORAGE_HREF + fileRelativePath
        + "?AccessId=datafier@email.com&Expires=123&Signature=lX";

    SubmittedFile file = SubmittedFile.builder().build();
    JobStatusResponse jobStatusResponse = JobStatusResponse.builder()
        .summary(Summary.builder()
            .outputLocation(outputLocation)
            .build())
        .jobInfo(JobInfo.builder()
            .jobId(JOB_ID_1)
            .masterJobStatus(COMPLETED)
            .build())
        .build();
    RequestContext requestContext = getRequestContext();
    DelfiFile delfiFile = DelfiFile.builder()
        .signedUrl(fileSignedUrl)
        .build();

    String jobMetadata = getMetadataFileContent(DELFI_RECORD_ID_1);

    given(portalService.getFile(fileUri, AUTHORIZATION_TOKEN, PARTITION))
        .willReturn(delfiFile);
    given(restTemplate.getForObject(fileSignedUrl, String.class))
        .willReturn(jobMetadata);

    // when
    DelfiIngestedFile ingestedFile = submitService
        .getIngestedFile(file, jobStatusResponse, requestContext);

    // then
    then(ingestedFile).isEqualTo(DelfiIngestedFile.builder()
        .submittedFile(file)
        .recordId(DELFI_RECORD_ID_1)
        .build());

    InOrder inOrder = Mockito.inOrder(restTemplate, delfiIngestionClient, portalService);
    inOrder.verify(portalService).getFile(fileUri, AUTHORIZATION_TOKEN, PARTITION);
    inOrder.verify(restTemplate).getForObject(fileSignedUrl, String.class);
    inOrder.verifyNoMoreInteractions();
  }

  private static Stream<Arguments> awaitJobProvider() {
    return Stream.of(
        arguments(JOB_ID_1, Arrays.asList(RUNNING, COMPLETED), COMPLETED),
        arguments(JOB_ID_2, Arrays.asList(RUNNING, FAILED), FAILED),
        arguments(JOB_ID_3, Arrays.asList(RUNNING, RUNNING, RUNNING), RUNNING)
    );
  }

  private RequestContext getRequestContext() {
    Map<String, String> emails = new HashMap<>();
    emails.put(DATA_DEFAULT_OWNERS, OWNER_EMAIL_1);
    emails.put(DATA_DEFAULT_VIEWERS, VIEWER_EMAIL_1);

    return RequestContext.builder()
        .authorizationToken(AUTHORIZATION_TOKEN)
        .partition(PARTITION)
        .userGroupEmailByName(emails)
        .build();
  }

  private JobStatusResponse getJobStatusResponse(String jobId, MasterJobStatus jobStatus) {
    return JobStatusResponse.builder()
        .jobInfo(JobInfo.builder()
            .jobId(jobId)
            .masterJobStatus(jobStatus)
            .build())
        .build();
  }

  private String getAcl(String ownerEmail, String viewerEmail) {
    return format("{\"acl\":{\"owners\":[\"%s\"],\"viewers\":[\"%s\"]}}", ownerEmail, viewerEmail);
  }

  private String getMetadataFileContent(String recordId) {
    return format("{\"status\":201,\"message\":\"{\\\"recordCount\\\":1,"
        + "\\\"recordIds\\\":[\\\"%s\\\"],\\\"skippedRecordIds\\\":[]}\"}", recordId);
  }

  private SubmitFileObject getSubmitFileObject(SubmitFileContext fileContext, String ingestor) {
    return SubmitFileObject.builder()
        .kind(fileContext.getKind())
        .acl(getAcl(OWNER_EMAIL_1, VIEWER_EMAIL_1))
        .filePath(GCS_PROTOCOL + fileContext.getRelativeFilePath())
        .fileInput(FileInput.FILE_PATH)
        .ingestorRoutines(ingestor)
        .build();
  }

}