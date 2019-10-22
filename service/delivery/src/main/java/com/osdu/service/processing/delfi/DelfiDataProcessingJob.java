package com.osdu.service.processing.delfi;

import com.osdu.model.FileRecord;
import com.osdu.model.Record;
import com.osdu.model.SrnToRecord;
import com.osdu.model.osdu.delivery.delfi.ProcessingResult;
import com.osdu.model.osdu.delivery.delfi.ProcessingResultStatus;
import com.osdu.service.PortalService;
import com.osdu.service.SrnMappingService;
import com.osdu.service.processing.DataProcessingJob;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DelfiDataProcessingJob implements DataProcessingJob {

  public static final String FILE_LOCATION_KEY = "signedUrl";
  public static final String LOCATION_KEY = "location";
  final String srn;
  final SrnMappingService srnMappingService;
  final PortalService portalService;
  final String authorizationToken;
  final String partition;

  @Override
  public ProcessingResult call() {

    ProcessingResult result = new ProcessingResult();
    result.setSrn(srn);

    SrnToRecord srnToRecord = srnMappingService.getSrnToRecord(srn);
    if (srnToRecord == null) {
      result.setProcessingResultStatus(ProcessingResultStatus.NO_MAPPING);
      return result;
    }
    String recordId = srnToRecord.getRecordId();
    final Record record = portalService.getRecord(recordId, authorizationToken, partition);
    if (record.getData().containsKey(LOCATION_KEY)) {
      final FileRecord file = portalService
          .getFile((record.getData()).get(LOCATION_KEY).toString(), authorizationToken, partition);
      result.setProcessingResultStatus(ProcessingResultStatus.FILE);
      result.setData(file);
      result.setFileLocation(String.valueOf(file.getAdditionalProperties().get(FILE_LOCATION_KEY)));
    } else {
      result.setData(record);
      result.setProcessingResultStatus(ProcessingResultStatus.DATA);
    }
    return result;
  }
}