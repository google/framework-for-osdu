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

import static com.osdu.service.JsonUtils.toJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.when;

import com.osdu.client.DelfiIngestionClient;
import com.osdu.model.RequestContext;
import com.osdu.model.SchemaData;
import com.osdu.model.delfi.Acl;
import com.osdu.model.delfi.status.JobInfo;
import com.osdu.model.delfi.status.JobPollingResult;
import com.osdu.model.delfi.status.JobStatusResponse;
import com.osdu.model.delfi.status.MasterJobStatus;
import com.osdu.model.delfi.submit.AclObject;
import com.osdu.model.delfi.submit.FileInput;
import com.osdu.model.delfi.submit.SubmitFileContext;
import com.osdu.model.delfi.submit.SubmitFileObject;
import com.osdu.model.delfi.submit.SubmitFileResult;
import com.osdu.model.delfi.submit.SubmitJobResult;
import com.osdu.model.delfi.submit.ingestor.LasIngestor;
import com.osdu.model.delfi.submit.ingestor.LasIngestorRoutine;
import com.osdu.model.property.DelfiPortalProperties;
import com.osdu.model.property.SubmitProperties;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DelfiSubmitServiceTest {

  private static final String AUTHORIZATION_TOKEN = "authToken";
  private static final String PARTITION = "partition";
  private static final String JOB_ID = "jobId";
  private static final String SRN = "srn";
  private static final String APP_KEY = "appKey";
  private static final String JOB_ID_1 = "jobId-1";
  private static final String JOB_ID_2 = "jobId-2";
  private static final String JOB_ID_3 = "jobId-3";
  private static final String EMAIL_1 = "email1";
  private static final String EMAIL_2 = "email2";
  private static final String DATA_DEFAULT_OWNERS = "data.default.owners";
  private static final String DATA_DEFAULT_VIEWERS = "data.default.viewers";

  private DelfiPortalProperties portalProperties = DelfiPortalProperties.builder()
      .appKey(APP_KEY)
      .build();
  private SubmitProperties submitProperties = SubmitProperties.builder()
      .pollingInterval(1000)
      .pollingCycles(180)
      .build();

  @Mock
  private DelfiIngestionClient delfiIngestionClient;

  private DelfiSubmitService delfiSubmitService;

  @Before
  public void setUp() {
    delfiSubmitService = new DelfiSubmitService(portalProperties, submitProperties,
        null, delfiIngestionClient, null);
  }

  @Test
  public void shouldWaitTillSubmitJobsDone() {

    // given
    RequestContext requestContext = RequestContext.builder()
        .authorizationToken(AUTHORIZATION_TOKEN)
        .partition(PARTITION)
        .build();

    List<String> jobIds = Arrays.asList(JOB_ID_1, JOB_ID_2, JOB_ID_3);

    when(delfiIngestionClient.getJobStatus(eq(JOB_ID_1), eq(AUTHORIZATION_TOKEN),
        eq(APP_KEY), eq(PARTITION)))
        .thenReturn(buildJobResponse(JOB_ID_1, MasterJobStatus.RUNNING))
        .thenReturn(buildJobResponse(JOB_ID_1, MasterJobStatus.COMPLETED));

    // when
    JobPollingResult jobsPullingResult = delfiSubmitService.awaitSubmitJob(JOB_ID_1, requestContext);

    // then
    assertThat(jobsPullingResult.getRunningJob()).isEqualTo(JOB_ID_1);
    assertThat(jobsPullingResult.getStatus()).isEqualTo(MasterJobStatus.COMPLETED);
  }

  @Test
  public void shouldSubmitFile() {
    // given
    SchemaData data = new SchemaData();

    Map<String, String> emails = new HashMap<>();
    emails.put(DATA_DEFAULT_OWNERS, EMAIL_1);
    emails.put(DATA_DEFAULT_VIEWERS, EMAIL_2);

    RequestContext requestContext = RequestContext.builder()
        .userGroupEmailByName(emails)
        .authorizationToken(AUTHORIZATION_TOKEN)
        .partition(PARTITION)
        .build();

    String filePath = "/test-path";
    String ingestorRoutines = toJson(Collections.singletonList(LasIngestorRoutine.builder()
        .lasIngestor(LasIngestor.builder()
            .createRawWellRecord(true)
            .build())
        .build()));

    SubmitFileContext fileContext = SubmitFileContext.builder()
        .relativeFilePath(filePath)
        .kind("")
        .wpcResourceTypeId("srn:type:work-product-component/WellLog:version1")
        .fileResourceTypeId("srn:type:file/las2:version1")
        .build();

    when(delfiIngestionClient.submitFile(eq(AUTHORIZATION_TOKEN), eq(APP_KEY), eq(PARTITION), eq(PARTITION),
        refEq(SubmitFileObject.builder()
            .kind("")
            .filePath("gs://test-path")
            .legalTags(requestContext.getLegalTags())
            .acl(getAcl())
            .fileInput(FileInput.FILE_PATH)
            .ingestorRoutines(ingestorRoutines)
            .build(), "additionalProperties")))
        .thenReturn(SubmitFileResult.builder().jobId(JOB_ID).build());

    // when
    SubmitJobResult submitJobResult = delfiSubmitService.submitFile(fileContext, requestContext);

    // then
    assertThat(submitJobResult.getJobId()).isEqualTo(JOB_ID);
  }

  private String getAcl() {
    return toJson(AclObject.builder()
        .acl(Acl.builder()
            .owner(EMAIL_1)
            .viewer(EMAIL_2)
            .build())
        .build());
  }

  private JobStatusResponse buildJobResponse(String jobId, MasterJobStatus status) {

    JobInfo jobInfo = new JobInfo();
    jobInfo.setJobId(jobId);
    jobInfo.setMasterJobStatus(status);

    JobStatusResponse jobStatusResponse = new JobStatusResponse();
    jobStatusResponse.setJobInfo(jobInfo);

    return jobStatusResponse;
  }
}
