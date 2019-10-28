package com.osdu.service.delfi;

import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.osdu.config.SenderConfiguration.PubSubIngestGateway;
import com.osdu.exception.IngestException;
import com.osdu.model.IngestResult;
import com.osdu.model.job.IngestMessage;
import com.osdu.model.manifest.LoadManifest;
import com.osdu.service.IngestService;
import com.osdu.service.JobStatusService;
import com.osdu.service.processing.InnerIngestionProcess;
import com.osdu.service.validation.LoadManifestValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DelfiIngestService implements IngestService {

  final JobStatusService jobStatusService;
  final InnerIngestionProcess innerIngestionProcess;
  final LoadManifestValidationService loadManifestValidationService;
  final PubSubIngestGateway ingestGateway;

  @Override
  public IngestResult ingestManifest(LoadManifest loadManifest,
      MessageHeaders headers) {
    log.info("Request to ingest file with following parameters: {}, and headers : {}", loadManifest,
        headers);

    final ProcessingReport validationResult = loadManifestValidationService
        .validateManifest(loadManifest);

    if (!validationResult.isSuccess()) {
      throw new IngestException(String
          .format("Failed to validate json from manifest %s, validation result is %s", loadManifest,
              validationResult));
    }

    String jobId = jobStatusService.initInjectJob();

    ingestGateway.sendIngestToPubSub(IngestMessage.builder()
        .ingestJobId(jobId)
        .loadManifest(loadManifest)
        .headers(headers)
        .build());

    log.info("Request to ingest with parameters : {}, init the injection jobId: {}", loadManifest,
        jobId);
    return IngestResult.builder()
        .jobId(jobId)
        .build();
  }
}
