package com.osdu.service.processing.delfi;

import com.osdu.model.osdu.delivery.delfi.ProcessingResult;
import com.osdu.model.osdu.delivery.delfi.ProcessingResultStatus;
import com.osdu.model.osdu.delivery.dto.DeliveryResponse;
import com.osdu.model.osdu.delivery.dto.ResponseItem;
import com.osdu.service.processing.ResultDataConverter;
import com.osdu.service.processing.ResultDataPostProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DelfiResultDataConverter implements ResultDataConverter {

    @Inject
    ResultDataPostProcessor resultDataPostProcessor;

    @Override
    public DeliveryResponse convertProcessingResults(List<ProcessingResult> results) {
        log.debug("Processing results : {}", results);

        Map<Boolean, List<ProcessingResult>> precessedToResultMap = results.stream()
                .collect(Collectors.partitioningBy(
                        result -> !result.getProcessingResultStatus().equals(ProcessingResultStatus.NO_MAPPING)));

        List<String> unprocessedSrns = precessedToResultMap.get(Boolean.FALSE).stream().
                map(ProcessingResult::getSrn)
                .collect(Collectors.toList());

        List<ResponseItem> responseItems = precessedToResultMap.get(Boolean.TRUE).stream()
                .map(result -> ResponseItem.builder()
                        .fileLocation(result.getFileLocation())
                        .data(result.getData())
                        .srn(result.getSrn()).build())
                .collect(Collectors.toList());

        responseItems.forEach(result -> resultDataPostProcessor.processData(result.getData()));

        DeliveryResponse response = new DeliveryResponse();
        response.setResult(responseItems);
        response.setUnprocessedSRNs(unprocessedSrns);

        return response;
    }

}
