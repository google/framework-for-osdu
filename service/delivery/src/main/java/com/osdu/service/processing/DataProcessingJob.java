package com.osdu.service.processing;

import com.osdu.model.osdu.delivery.FileRecord;
import com.osdu.model.osdu.delivery.Record;
import com.osdu.model.osdu.delivery.delfi.ProcessingResult;
import com.osdu.model.osdu.delivery.delfi.ProcessingResultStatus;
import com.osdu.service.DeliveryService;
import com.osdu.service.SRNMappingService;
import com.osdu.service.delfi.DelfiDeliveryPortalService;
import com.osdu.service.delfi.DelfiDeliveryService;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.concurrent.Callable;

@RequiredArgsConstructor
public class DataProcessingJob implements Callable {

    private static final String FILE_LOCATION_KEY = "fileLocation";
    private final String srn;
    private final SRNMappingService srnMappingService;
    private final DelfiDeliveryPortalService deliveryService;
    private final String authorizationToken;
    private final String partition;

    @Override
    public ProcessingResult call() throws Exception {

        ProcessingResult result = new ProcessingResult();
        result.setSrn(srn);

        String odesId = srnMappingService.mapSRNToKind(srn);
        if (odesId == null) {
            result.setProcessingResultStatus(ProcessingResultStatus.NO_MAPPING);
            return result;
        }
        final Record record = deliveryService.getRecord(odesId, authorizationToken, partition);
        //TODO: Beautify this
        if (((Map<String, Object>) record.getData().get("data")).containsKey("location")) {
            final FileRecord file = deliveryService.getFile(((Map<String, Object>) record.getData().get("data")).get("location").toString(), authorizationToken, partition);
            result.setProcessingResultStatus(ProcessingResultStatus.FILE);
            result.setData(file.getData());
            result.setFileLocation(String.valueOf(file.getData().get(FILE_LOCATION_KEY)));
        } else {
            result.setData(record.getData());
            result.setProcessingResultStatus(ProcessingResultStatus.DATA);
        }
        return result;
    }
}