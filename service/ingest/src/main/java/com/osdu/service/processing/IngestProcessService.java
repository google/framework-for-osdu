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

package com.osdu.service.processing;

import com.osdu.model.IngestHeaders;
import com.osdu.model.Record;
import com.osdu.model.RequestContext;
import com.osdu.model.ingest.IngestedFile;
import com.osdu.model.ingest.IngestedWp;
import com.osdu.model.ingest.IngestedWpc;
import com.osdu.model.job.IngestJob;
import com.osdu.model.job.IngestJobStatus;
import com.osdu.model.type.manifest.LoadManifest;
import com.osdu.model.type.manifest.ManifestFile;
import com.osdu.model.type.manifest.ManifestWp;
import com.osdu.model.type.manifest.ManifestWpc;
import com.osdu.service.AuthenticationService;
import com.osdu.service.IngestionService;
import com.osdu.service.JobStatusService;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class IngestProcessService {

  final AuthenticationService authenticationService;
  final JobStatusService jobStatusService;
  final IngestWpService ingestWpService;
  final IngestionService ingestionService;

  /**
   * Process load manifest. It includes ingestion files, work product components and work product.
   * @param ingestJobId ingest job ID
   * @param loadManifest load manifest
   * @param headers ingest headers
   */
  public void processLoadManifest(String ingestJobId, LoadManifest loadManifest,
      IngestHeaders headers) {
    log.debug("Start the internal injection process. JobId: {}, loadManifest: {}, headers: {}",
        ingestJobId, loadManifest, headers);
    RequestContext requestContext = getRequestContext(headers);
    log.debug("Ingestion request context: {}", requestContext);

    log.debug("Start ingest work product.");
    ManifestWp workProduct = getWorkProduct(loadManifest);
    IngestedWp ingestedWp = ingestWpService.processWp(workProduct, requestContext);
    log.debug("Work product is ingested: {}", ingestedWp);

    log.debug("Save ingest job result.");
    IngestJob ingestJob = getResultIngestJob(ingestedWp);

    // fail created records if WP has been unsuccessfully processed
    if (!ingestedWp.isSuccess()) {
      List<Record> recordsInWp = getWpRecordStream(ingestedWp)
          .collect(Collectors.toList());
      ingestionService.failRecords(recordsInWp, requestContext);
    }

    jobStatusService.save(ingestJob.toBuilder()
        .id(ingestJobId)
        .build());
    log.debug("Finished the internal async injection process. Ingest job: {}", ingestJob);
  }

  private RequestContext getRequestContext(IngestHeaders headers) {
    String authorizationToken = headers.getAuthorizationToken();
    String partition = headers.getPartition();

    Map<String, String> groupEmailByName = authenticationService
        .getGroupEmailToName(authorizationToken, partition);

    return RequestContext.builder()
        .authorizationToken(authorizationToken)
        .partition(partition)
        .legalTags(headers.getLegalTags())
        .userGroupEmailByName(groupEmailByName)
        .headers(headers)
        .build();
  }

  private static List<ManifestWpc> getWorkProductComponents(LoadManifest loadManifest) {
    Map<String, ManifestFile> fileById = loadManifest.getFiles().stream()
        .collect(Collectors.toMap(ManifestFile::getAssociativeId, Function.identity()));
    return loadManifest.getWorkProductComponents().stream()
        .map(wpc -> {
          wpc.setFiles(wpc.getFileAssociativeIds().stream()
              .map(fileById::get)
              .map(file -> {
                file.setWpc(wpc);
                return file;
              })
              .collect(Collectors.toList()));
          return wpc;
        })
        .collect(Collectors.toList());
  }

  private static ManifestWp getWorkProduct(LoadManifest loadManifest) {
    ManifestWp workProduct = loadManifest.getWorkProduct();
    workProduct.setManifestWpcs(getWorkProductComponents(loadManifest));
    return workProduct;
  }

  private static IngestJob getResultIngestJob(IngestedWp ingestedWp) {
    if (ingestedWp.isSuccess()) {
      return getCompleteIngestJob(ingestedWp);
    }

    return getFailedIngestJob(ingestedWp);
  }

  private static IngestJob getCompleteIngestJob(IngestedWp wp) {
    List<String> srnsInWp = getWpSrnStream(wp)
        .collect(Collectors.toList());

    return  IngestJob.builder()
        .status(IngestJobStatus.COMPLETE)
        .srns(srnsInWp)
        .summary("Ingestion successfully completed. Created " + srnsInWp.size() + " records.")
        .build();
  }

  private static IngestJob getFailedIngestJob(IngestedWp wp) {
    List<String> srnsInWp = getWpSrnStream(wp)
        .collect(Collectors.toList());

    String wpcsIngestedSummary = getFailedSummary(wp, srnsInWp.size());

    return IngestJob.builder()
        .status(IngestJobStatus.FAILED)
        .srns(srnsInWp)
        .summary(wpcsIngestedSummary)
        .build();
  }

  private static Stream<String> getWpSrnStream(IngestedWp wp) {
    Stream<String> wpcSrnStream = wp.getIngestedWpcs().stream()
        .flatMap(IngestProcessService::getWpcSrnStream);
    return Stream.concat(Stream.of(wp.getSrn()), wpcSrnStream)
        .filter(Objects::nonNull);
  }

  private static Stream<String> getWpcSrnStream(IngestedWpc wpc) {
    Stream<String> srnStream = wpc.getIngestedFiles().stream()
        .map(IngestedFile::getSrn);
    return Stream.concat(Stream.of(wpc.getSrn()), srnStream);
  }

  private static String getFailedSummary(IngestedWp wp, int createdRecords) {
    String failedMsg = String.format("Ingestion failed. It was possible to create %s records.",
        createdRecords);

    return Stream.concat(Stream.of(failedMsg), wp.getSummaries().stream())
        .filter(Objects::nonNull)
        .collect(Collectors.joining(System.lineSeparator()));
  }

  private static Stream<Record> getWpRecordStream(IngestedWp wp) {
    Stream<Record> recordStream = wp.getIngestedWpcs().stream()
        .flatMap(IngestProcessService::getWpcRecordStream);
    return Stream.concat(Stream.of(wp.getWpRecord()), recordStream)
        .filter(Objects::nonNull);
  }

  private static Stream<Record> getWpcRecordStream(IngestedWpc wpc) {
    Stream<Record> fileRecordStream = wpc.getIngestedFiles().stream()
        .map(IngestedFile::getRecord);
    return Stream.concat(Stream.of(wpc.getWpcRecord()), fileRecordStream);
  }

}
