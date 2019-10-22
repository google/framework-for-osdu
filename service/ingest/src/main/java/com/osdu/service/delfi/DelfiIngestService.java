package com.osdu.service.delfi;

import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.osdu.exception.IngestException;
import com.osdu.model.IngestResult;
import com.osdu.model.manifest.LoadManifest;
import com.osdu.service.IngestService;
import com.osdu.service.JobStatusService;
import com.osdu.service.processing.InnerInjectionProcess;
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
  final InnerInjectionProcess innerInjectionProcess;
  final LoadManifestValidationService loadManifestValidationService;

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

    innerInjectionProcess.process(jobId, loadManifest, headers);

    log.info("Request to ingest with parameters : {}, init the injection jobId: {}", loadManifest,
        jobId);
    return IngestResult.builder()
        .jobId(jobId)
        .build();
  }
}
