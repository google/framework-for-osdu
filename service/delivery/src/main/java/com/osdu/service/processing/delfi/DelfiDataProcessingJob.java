package com.osdu.service.processing.delfi;

import com.osdu.model.SchemaData;
import com.osdu.model.osdu.delivery.FileRecord;
import com.osdu.model.osdu.delivery.Record;
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

    final SchemaData schemaDataForSrn = srnMappingService.getSchemaDataForSrn(srn);
    String kind = schemaDataForSrn.getKind();
    if (kind == null) {
      result.setProcessingResultStatus(ProcessingResultStatus.NO_MAPPING);
      return result;
    }
    final Record record = portalService.getRecord(kind, authorizationToken, partition);
    if (record.getData().containsKey(LOCATION_KEY)) {
      final FileRecord file = portalService
          .getFile((record.getData()).get(LOCATION_KEY).toString(), authorizationToken, partition);
      result.setProcessingResultStatus(ProcessingResultStatus.FILE);
      result.setData(file);
      result.setFileLocation(String.valueOf(file.getDetails().get(FILE_LOCATION_KEY)));
    } else {
      result.setData(record);
      result.setProcessingResultStatus(ProcessingResultStatus.DATA);
    }
    return result;
  }
}