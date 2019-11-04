package com.osdu.service.processing.delfi;

import com.osdu.model.FileRecord;
import com.osdu.model.Record;
import com.osdu.model.SrnToRecord;
import com.osdu.model.delfi.DelfiFile;
import com.osdu.model.osdu.delivery.delfi.ProcessingResult;
import com.osdu.model.osdu.delivery.delfi.ProcessingResultStatus;
import com.osdu.service.PortalService;
import com.osdu.service.SrnMappingService;
import com.osdu.service.processing.DataProcessingJob;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DelfiDataProcessingJob implements DataProcessingJob {

  public static final String FILE_LOCATION_KEY = "signedUrl";
  public static final String LOCATION_KEY = "location";

  final SrnMappingService srnMappingService;
  final PortalService portalService;

  /**
   * Perform the async data processing for delivery for specified SRN.
   * It's run on {@code AsyncConfiguration#dataProcessingExecutor} executor.
   *
   * @param srn SRN
   * @param authorizationToken Bearer token
   * @param partition partition
   * @return {@link CompletableFuture} of delivery data processing result.
   */
  @Async("dataProcessingExecutor")
  public CompletableFuture<ProcessingResult> process(String srn, String authorizationToken,
      String partition) {

    ProcessingResult result = new ProcessingResult();
    result.setSrn(srn);

    SrnToRecord srnToRecord = srnMappingService.getSrnToRecord(srn);
    if (srnToRecord == null) {
      result.setProcessingResultStatus(ProcessingResultStatus.NO_MAPPING);
      return CompletableFuture.completedFuture(result);
    }
    String recordId = srnToRecord.getRecordId();
    final Record record = portalService.getRecord(recordId, authorizationToken, partition);
    if (record.getData().containsKey(LOCATION_KEY)) {
      DelfiFile file = portalService
          .getFile(record.getData().get(LOCATION_KEY).toString(), authorizationToken, partition);
      result.setProcessingResultStatus(ProcessingResultStatus.FILE);
      result.setData(new FileRecord());
      result.setFileLocation(file.getSignedUrl());
    } else {
      result.setData(record);
      result.setProcessingResultStatus(ProcessingResultStatus.DATA);
    }
    return CompletableFuture.completedFuture(result);
  }
}