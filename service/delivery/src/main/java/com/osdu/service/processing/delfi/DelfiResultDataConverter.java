package com.osdu.service.processing.delfi;

import com.osdu.model.osdu.delivery.delfi.ProcessingResult;
import com.osdu.model.osdu.delivery.delfi.ProcessingResultStatus;
import com.osdu.model.osdu.delivery.dto.DeliveryResponse;
import com.osdu.model.osdu.delivery.dto.ResponseItem;
import com.osdu.service.processing.ResultDataConverter;
import com.osdu.service.processing.ResultDataProcessor;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DelfiResultDataConverter implements ResultDataConverter {

  final ResultDataProcessor resultDataProcessor;

  @Override
  public DeliveryResponse convertProcessingResults(List<ProcessingResult> results) {
    log.debug("Processing results : {}", results);

    Map<Boolean, List<ProcessingResult>> precessedToResultMap = results.stream()
        .collect(Collectors.partitioningBy(
            result -> !result.getProcessingResultStatus()
                .equals(ProcessingResultStatus.NO_MAPPING)));

    List<String> unprocessedSrns = precessedToResultMap.get(Boolean.FALSE).stream()
        .map(ProcessingResult::getSrn)
        .collect(Collectors.toList());

    List<ResponseItem> responseItems = precessedToResultMap.get(Boolean.TRUE).stream()
        .map(result -> ResponseItem.builder()
            .fileLocation(result.getFileLocation())
            .data(result.getData())
            .srn(result.getSrn()).build())
        .peek(result -> resultDataProcessor.removeRedundantFields(result.getData()))
        .collect(Collectors.toList());

    return DeliveryResponse.builder()
        .result(responseItems)
        .unprocessedSrns(unprocessedSrns).build();
  }
}
