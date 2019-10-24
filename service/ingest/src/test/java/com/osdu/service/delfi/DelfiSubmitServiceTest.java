package com.osdu.service.delfi;

import static com.osdu.service.JsonUtils.toJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.when;

import com.osdu.client.DelfiIngestionClient;
import com.osdu.model.ResourceTypeId;
import com.osdu.model.SchemaData;
import com.osdu.model.delfi.Acl;
import com.osdu.model.delfi.RequestMeta;
import com.osdu.model.delfi.status.JobInfo;
import com.osdu.model.delfi.status.JobStatusResponse;
import com.osdu.model.delfi.status.JobsPullingResult;
import com.osdu.model.delfi.status.MasterJobStatus;
import com.osdu.model.delfi.submit.AclObject;
import com.osdu.model.delfi.submit.FileInput;
import com.osdu.model.delfi.submit.SubmitFileObject;
import com.osdu.model.delfi.submit.SubmitFileResult;
import com.osdu.model.delfi.submit.SubmitJobResult;
import com.osdu.model.delfi.submit.ingestor.LasIngestor;
import com.osdu.model.delfi.submit.ingestor.LasIngestorObject;
import com.osdu.model.property.DelfiPortalProperties;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
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

  @Mock
  private DelfiPortalProperties portalProperties;
  @Mock
  private DelfiIngestionClient delfiIngestionClient;

  @InjectMocks
  private DelfiSubmitService delfiSubmitService;

  @Test
  public void shouldWaitTillSubmitJobsDone() {

    // given
    RequestMeta requestMeta = RequestMeta.builder().authorizationToken(AUTHORIZATION_TOKEN)
        .partition(PARTITION).build();

    List<String> jobIds = Arrays.asList(JOB_ID_1, JOB_ID_2, JOB_ID_3);

    when(portalProperties.getAppKey()).thenReturn(APP_KEY);

    when(delfiIngestionClient.getJobStatus(eq(JOB_ID_1), eq(AUTHORIZATION_TOKEN),
        eq(APP_KEY), eq(PARTITION)))
        .thenReturn(buildJobResponse(JOB_ID_1, MasterJobStatus.RUNNING))
        .thenReturn(buildJobResponse(JOB_ID_1, MasterJobStatus.COMPLETED));

    when(delfiIngestionClient.getJobStatus(eq(JOB_ID_2), eq(AUTHORIZATION_TOKEN),
        eq(APP_KEY), eq(PARTITION)))
        .thenReturn(buildJobResponse(JOB_ID_2, MasterJobStatus.COMPLETED));

    when(delfiIngestionClient.getJobStatus(eq(JOB_ID_3), eq(AUTHORIZATION_TOKEN),
        eq(APP_KEY), eq(PARTITION)))
        .thenReturn(buildJobResponse(JOB_ID_3, MasterJobStatus.RUNNING))
        .thenReturn(buildJobResponse(JOB_ID_3, MasterJobStatus.RUNNING))
        .thenReturn(buildJobResponse(JOB_ID_3, MasterJobStatus.FAILED));

    // when
    JobsPullingResult jobsPullingResult = delfiSubmitService.awaitSubmitJobs(jobIds, requestMeta);

    // then
    assertThat(jobsPullingResult.getRunningJobs()).isEqualTo(jobIds);
    assertThat(jobsPullingResult.getCompletedJobs())
        .extracting(response -> response.getJobInfo().getJobId())
        .containsExactlyInAnyOrder(JOB_ID_1, JOB_ID_2);
    assertThat(jobsPullingResult.getFailedJobs())
        .extracting(response -> response.getJobInfo().getJobId())
        .containsExactly(JOB_ID_3);
  }

  @Test
  public void shouldSubmitFile() {

    // given
    SchemaData data = new SchemaData();
    data.setSrn(SRN);

    Map<String, String> emails = new HashMap<>();
    emails.put(DATA_DEFAULT_OWNERS, EMAIL_1);
    emails.put(DATA_DEFAULT_VIEWERS, EMAIL_2);

    RequestMeta requestMeta = RequestMeta.builder().schemaData(data)
        .resourceTypeId(new ResourceTypeId("srn:type:work-product-component/WellLog:version1"))
        .userGroupEmailByName(emails)
        .authorizationToken(AUTHORIZATION_TOKEN)
        .partition(PARTITION).build();

    String filePath = "/test-path";
    String ingestorRoutines = toJson(Collections.singletonList(LasIngestorObject.builder()
        .lasIngestor(LasIngestor.builder().build())
        .build()));

    when(portalProperties.getAppKey()).thenReturn(APP_KEY);

    when(portalProperties.getAppKey()).thenReturn(APP_KEY);
    when(delfiIngestionClient.submitFile(eq(AUTHORIZATION_TOKEN), eq(APP_KEY), eq(PARTITION),
        refEq(SubmitFileObject.builder()
            .kind(requestMeta.getSchemaData().getKind())
            .filePath("gs://test-path")
            .legalTags(requestMeta.getLegalTags())
            .acl(getAcl())
            .fileInput(FileInput.FILE_PATH)
            .ingestorRoutines(ingestorRoutines)
            .build(), "additionalProperties")))
        .thenReturn(SubmitFileResult.builder().jobId(JOB_ID).build());

    // when
    SubmitJobResult submitJobResult = delfiSubmitService.submitFile(filePath, requestMeta);

    // then
    assertThat(submitJobResult.getJobId()).isEqualTo(JOB_ID);
    assertThat(submitJobResult.getSrn()).contains(SRN);
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
