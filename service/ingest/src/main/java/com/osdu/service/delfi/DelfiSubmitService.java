package com.osdu.service.delfi;

import static com.osdu.model.delfi.status.MasterJobStatus.COMPLETED;
import static com.osdu.model.delfi.status.MasterJobStatus.FAILED;
import static com.osdu.model.delfi.status.MasterJobStatus.RUNNING;
import static com.osdu.service.JsonUtils.toJson;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.osdu.client.DelfiIngestionClient;
import com.osdu.exception.IngestException;
import com.osdu.model.delfi.RequestMeta;
import com.osdu.model.delfi.status.JobStatus;
import com.osdu.model.delfi.status.JobStatusResponse;
import com.osdu.model.delfi.status.JobsPullingResult;
import com.osdu.model.delfi.status.MasterJobStatus;
import com.osdu.model.delfi.submit.Acl;
import com.osdu.model.delfi.submit.AclObject;
import com.osdu.model.delfi.submit.FileInput;
import com.osdu.model.delfi.submit.SubmitFileObject;
import com.osdu.model.delfi.submit.SubmitFileResult;
import com.osdu.model.delfi.submit.SubmitJobResult;
import com.osdu.model.property.DelfiPortalProperties;
import com.osdu.service.SubmitService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DelfiSubmitService implements SubmitService {

  final DelfiPortalProperties portalProperties;
  final ObjectMapper objectMapper;
  final DelfiIngestionClient delfiIngestionClient;

  @Override
  public JobsPullingResult awaitSubmitJobs(List<String> jobIds, RequestMeta requestMeta) {
    List<String> runningJobs = jobIds;
    List<JobStatus> failedJobs = new ArrayList<>(runningJobs.size());
    List<JobStatus> completedJobs = new ArrayList<>(runningJobs.size());

    while (!runningJobs.isEmpty()) {
      Map<MasterJobStatus, List<JobStatus>> submittedJobsByStatus = runningJobs.stream()
          .map(jobId -> delfiIngestionClient.getJobStatus(jobId,
              requestMeta.getAuthorizationToken(),
              portalProperties.getAppKey(),
              requestMeta.getPartition()))
          .collect(groupingBy(response -> response.getStatus().getJobInfo().getMasterJobStatus(),
                  mapping(JobStatusResponse::getStatus, toList())));

      runningJobs = MapUtils.getObject(submittedJobsByStatus, RUNNING, emptyList()).stream()
          .map(status -> status.getJobInfo().getJobId())
          .collect(toList());
      failedJobs.addAll(MapUtils.getObject(submittedJobsByStatus, FAILED, emptyList()));
      completedJobs.addAll(MapUtils.getObject(submittedJobsByStatus, COMPLETED, emptyList()));

      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        throw new IngestException("Pulling submitted jobs was unexpected interrupted. JobIds = "
            + jobIds, e);
      }
    }

    return JobsPullingResult.builder()
        .runningJobs(jobIds)
        .failedJobs(failedJobs)
        .completedJobs(completedJobs)
        .build();
  }

  @Override
  public SubmitJobResult submitFile(String relativeFilePath, RequestMeta requestMeta) {
    String srn = generateSrn(requestMeta.getSchemaData().getSrn());
    SubmitFileResult submitFileResult = delfiIngestionClient
        .submitFile(requestMeta.getAuthorizationToken(),
            portalProperties.getAppKey(),
            requestMeta.getPartition(),
            SubmitFileObject.builder()
                .kind(requestMeta.getSchemaData().getKind())
                .filePath("gs://" + normalizeRelativePath(relativeFilePath))
                .legalTags(requestMeta.getLegalTags())
                .acl(getAcl(requestMeta.getUserGroupEmailByName()))
                .fileInput(FileInput.FILE_PATH)
                .additionalProperties(getAdditionalProperties(srn))
                .build());

    return SubmitJobResult.builder()
        .jobId(submitFileResult.getJobId())
        .srn(srn)
        .build();
  }

  private String generateSrn(String srn) {
    return srn + UUID.randomUUID().toString().replace("-", "");
  }

  private String normalizeRelativePath(String path) {
    return StringUtils.stripStart(path, "/");
  }

  private String getAcl(Map<String, String> groupEmailByName) {
    return toJson(AclObject.builder()
        .acl(Acl.builder()
            .owner(groupEmailByName.get("data.default.owners"))
            .viewer(groupEmailByName.get("data.default.viewers"))
            .build())
        .build());
  }

  private String getAdditionalProperties(String srn) {
    HashMap<String, String> properties = new HashMap<>();
    properties.put("srn", srn);

    return toJson(properties);
  }

}
