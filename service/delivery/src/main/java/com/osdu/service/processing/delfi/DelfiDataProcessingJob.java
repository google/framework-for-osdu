package com.osdu.service.processing.delfi;

import com.osdu.model.osdu.delivery.FileRecord;
import com.osdu.model.osdu.delivery.Record;
import com.osdu.model.osdu.delivery.delfi.ProcessingResult;
import com.osdu.model.osdu.delivery.delfi.ProcessingResultStatus;
import com.osdu.service.PortalService;
import com.osdu.service.SRNMappingService;
import com.osdu.service.processing.DataProcessingJob;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DelfiDataProcessingJob implements DataProcessingJob {

    static final String FILE_LOCATION_KEY = "signedUrl";
    static final String LOCATION_KEY = "location";
    final String srn;
    final SRNMappingService srnMappingService;
    final PortalService portalService;
    final String authorizationToken;
    final String partition;

    @Override
    public ProcessingResult call() {

        ProcessingResult result = new ProcessingResult();
        result.setSrn(srn);

        String odesId = srnMappingService.mapSRNToKind(srn);
        if (odesId == null) {
            result.setProcessingResultStatus(ProcessingResultStatus.NO_MAPPING);
            return result;
        }
        final Record record = portalService.getRecord(odesId, authorizationToken, partition);
        if (record.getData().containsKey(LOCATION_KEY)) {
            final FileRecord file = portalService.getFile((record.getData()).get(LOCATION_KEY).toString(), authorizationToken, partition);
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