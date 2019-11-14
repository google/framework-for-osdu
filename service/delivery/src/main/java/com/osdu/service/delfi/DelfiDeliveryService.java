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

package com.osdu.service.delfi;

import static com.osdu.request.OsduHeader.extractHeaderByName;

import com.osdu.exception.OsduException;
import com.osdu.model.osdu.delivery.delfi.ProcessingResult;
import com.osdu.model.osdu.delivery.dto.DeliveryResponse;
import com.osdu.model.osdu.delivery.input.InputPayload;
import com.osdu.model.osdu.delivery.property.OsduDeliveryProperties;
import com.osdu.request.OsduHeader;
import com.osdu.service.AuthenticationService;
import com.osdu.service.DeliveryService;
import com.osdu.service.PortalService;
import com.osdu.service.SrnMappingService;
import com.osdu.service.processing.DataProcessingJob;
import com.osdu.service.processing.ResultDataConverter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DelfiDeliveryService implements DeliveryService {

  final SrnMappingService srnMappingService;
  final PortalService portalService;
  final ResultDataConverter resultDataConverter;
  final AuthenticationService authenticationService;
  final OsduDeliveryProperties properties;
  final DataProcessingJob dataProcessingJob;

  @Override
  public DeliveryResponse getResources(InputPayload inputPayload, MessageHeaders headers) {
    log.debug("Getting resources for following SRNs and headers : {}, {}", inputPayload, headers);

    String authorizationToken = extractHeaderByName(headers, OsduHeader.AUTHORIZATION);
    String partition = extractHeaderByName(headers, OsduHeader.PARTITION);

    authenticationService.checkAuthentication(authorizationToken, partition);

    Map<String, Future<ProcessingResult>> srnToFutureMap = inputPayload.getSrns().stream()
        .collect(Collectors.toMap(Function.identity(),
            srn -> dataProcessingJob.process(srn, authorizationToken, partition)));

    List<ProcessingResult> results = srnToFutureMap.entrySet().stream()
        .map(this::getProcessingResult)
        .collect(Collectors.toList());

    return resultDataConverter.convertProcessingResults(results);
  }

  private ProcessingResult getProcessingResult(
      Entry<String, Future<ProcessingResult>> srnToFuture) {
    try {
      return srnToFuture.getValue().get();
    } catch (ExecutionException e) {
      throw new OsduException("Error execution srn - " + srnToFuture.getKey(), e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new OsduException("Error execution srn - " + srnToFuture.getKey(), e);
    }
  }

}

