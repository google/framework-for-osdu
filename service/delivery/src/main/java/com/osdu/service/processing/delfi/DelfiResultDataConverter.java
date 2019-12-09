/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.osdu.service.processing.delfi;

import com.osdu.model.osdu.delivery.delfi.ProcessingResult;
import com.osdu.model.osdu.delivery.delfi.ProcessingResultStatus;
import com.osdu.model.osdu.delivery.dto.DeliveryResponse;
import com.osdu.model.osdu.delivery.dto.ResponseFileLocation;
import com.osdu.model.osdu.delivery.dto.ResponseItem;
import com.osdu.service.processing.ResultDataConverter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DelfiResultDataConverter implements ResultDataConverter {

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
            .fileLocation(result.getFileLocation() == null? null :
                new ResponseFileLocation(result.getFileLocation()))
            .data(result.getData())
            .srn(result.getSrn()).build())
        .collect(Collectors.toList());

    return DeliveryResponse.builder()
        .result(responseItems)
        .unprocessedSrns(unprocessedSrns).build();
  }
}
