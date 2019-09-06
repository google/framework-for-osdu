package com.osdu.service.processing;

import com.osdu.model.osdu.delivery.process.ProcessingResult;
import com.osdu.service.SRNMappingService;
import com.osdu.service.StorageService;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.Callable;

@RequiredArgsConstructor
public class DataProcessingJob implements Callable {

    private final String srn;
    private final SRNMappingService srnMappingService;
    private final StorageService storageService;

    @Override
    public ProcessingResult call() throws Exception {

        ProcessingResult result = new ProcessingResult();
        result.setSrn(srn);

        String odesId = srnMappingService.mapSRNToKind(srn);
        if( odesId == null){
            result.setProcessed(false);
            return result;
        }

        String metadata = storageService.getMetadata(odesId);

        result.setProcessed(true);
        result.setData(metadata);

        return result;
    }
}