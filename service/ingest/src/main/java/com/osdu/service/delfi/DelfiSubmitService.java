package com.osdu.service.delfi;

import static com.osdu.model.delfi.status.MasterJobStatus.COMPLETED;
import static com.osdu.model.delfi.status.MasterJobStatus.FAILED;
import static com.osdu.model.delfi.status.MasterJobStatus.RUNNING;
import static com.osdu.service.JsonUtils.toJson;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import com.osdu.client.DelfiIngestionClient;
import com.osdu.exception.IngestException;
import com.osdu.model.ResourceTypeId;
import com.osdu.model.delfi.RequestMeta;
import com.osdu.model.delfi.status.JobInfo;
import com.osdu.model.delfi.status.JobStatusResponse;
import com.osdu.model.delfi.status.JobsPullingResult;
import com.osdu.model.delfi.status.MasterJobStatus;
import com.osdu.model.delfi.submit.Acl;
import com.osdu.model.delfi.submit.AclObject;
import com.osdu.model.delfi.submit.FileInput;
import com.osdu.model.delfi.submit.SubmitFileObject;
import com.osdu.model.delfi.submit.SubmitFileResult;
import com.osdu.model.delfi.submit.SubmitJobResult;
import com.osdu.model.delfi.submit.ingestor.LasIngestor;
import com.osdu.model.delfi.submit.ingestor.LasIngestorObject;
import com.osdu.model.property.DelfiPortalProperties;
import com.osdu.service.SubmitService;
import java.util.ArrayList;
import java.util.Collections;
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
  final DelfiIngestionClient delfiIngestionClient;

  @Override
  public JobsPullingResult awaitSubmitJobs(List<String> jobIds, RequestMeta requestMeta) {
    List<String> runningJobs = jobIds;
    List<JobInfo> failedJobs = new ArrayList<>(runningJobs.size());
    List<JobInfo> completedJobs = new ArrayList<>(runningJobs.size());

    while (!runningJobs.isEmpty()) {
      Map<MasterJobStatus, List<JobInfo>> submittedJobsByStatus = runningJobs.stream()
          .map(jobId -> delfiIngestionClient.getJobStatus(jobId,
              requestMeta.getAuthorizationToken(),
              portalProperties.getAppKey(),
              requestMeta.getPartition()))
          .collect(groupingBy(response -> response.getJobInfo().getMasterJobStatus(),
                  mapping(JobStatusResponse::getJobInfo, toList())));

      runningJobs = MapUtils.getObject(submittedJobsByStatus, RUNNING, emptyList()).stream()
          .map(JobInfo::getJobId)
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
    String srn = generateSrn(requestMeta.getResourceTypeId().getRaw());
    SubmitFileResult submitFileResult = delfiIngestionClient
        .submitFile(requestMeta.getAuthorizationToken(),
            portalProperties.getAppKey(),
            requestMeta.getPartition(),
            SubmitFileObject.builder()
                .kind(requestMeta.getSchemaData().getKind())
                .acl(getAcl(requestMeta.getUserGroupEmailByName()))
                .legalTags(requestMeta.getLegalTags())
                .filePath("gs://" + normalizeRelativePath(relativeFilePath))
                .fileInput(FileInput.FILE_PATH)
                .ingestorRoutines(getIngestorRoutines(requestMeta.getResourceTypeId()))
                .additionalProperties(getAdditionalProperties(srn))
                .build());

    return SubmitJobResult.builder()
        .jobId(submitFileResult.getJobId())
        .srn(srn)
        .build();
  }

  private String generateSrn(String resourceTypeId) {
    return resourceTypeId + UUID.randomUUID().toString().replace("-", "");
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

  private String getIngestorRoutines(ResourceTypeId resourceTypeId) {
    switch (resourceTypeId.getResourceType()) {
      case WPC_WELL_LOG:
        return toJson(Collections.singletonList(LasIngestorObject.builder()
            .lasIngestor(LasIngestor.builder()
                .build())
            .build()));
      default:
        return null;
    }
  }

  private String getAdditionalProperties(String srn) {
    HashMap<String, String> properties = new HashMap<>();
    properties.put("srn", srn);
    HashMap<String, Object> additionalProperties = new HashMap<>();
    additionalProperties.put("additionalProperties", properties);

    return toJson(additionalProperties);
  }

}
