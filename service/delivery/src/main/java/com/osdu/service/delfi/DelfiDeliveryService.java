package com.osdu.service.delfi;

import com.osdu.exception.OSDUException;
import com.osdu.model.osdu.delivery.delfi.ProcessingResult;
import com.osdu.model.osdu.delivery.dto.DeliveryResponse;
import com.osdu.model.osdu.delivery.input.InputPayload;
import com.osdu.service.DeliveryService;
import com.osdu.service.PortalService;
import com.osdu.service.SrnMappingService;
import com.osdu.service.processing.DataProcessingJob;
import com.osdu.service.processing.ResultDataConverter;
import com.osdu.service.processing.delfi.DelfiDataProcessingJob;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DelfiDeliveryService implements DeliveryService {

  static final String PARTITION_HEADER_KEY = "partition";
  static final String AUTHORIZATION_HEADER_KEY = "authorization";

  @Inject
  SrnMappingService srnMappingService;

  @Inject
  PortalService portalService;

  @Inject
  ResultDataConverter resultDataConverter;

  @Value("${osdu.processing.thread-pool-capacity}")
  int threadPoolCapacity;

  @Override
  public DeliveryResponse getResources(InputPayload inputPayload, MessageHeaders headers) {
    log.debug("Getting resources for following SRNs and headers : {}, {}", inputPayload, headers);
    ExecutorService executor = Executors.newFixedThreadPool(threadPoolCapacity);

    String authorizationToken = extractHeaders(headers, AUTHORIZATION_HEADER_KEY);
    String partition = extractHeaders(headers, PARTITION_HEADER_KEY);

    List<DataProcessingJob> jobs = inputPayload.getSrns().stream()
        .map(srn -> new DelfiDataProcessingJob(srn, srnMappingService, portalService,
            authorizationToken, partition))
        .collect(Collectors.toList());

    List<Future<ProcessingResult>> futures = new ArrayList<>();
    for (DataProcessingJob job : jobs) {
      futures.add(executor.submit(job));
    }

    List<ProcessingResult> results = new ArrayList<>();
    for (Future<ProcessingResult> job : futures) {
      try {
        results.add(job.get());
      } catch (ExecutionException e) {
        log.error("Error execution srn", e);
        throw new OSDUException("Error execution srn");
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    executor.shutdown();

    return resultDataConverter.convertProcessingResults(results);
  }

  private String extractHeaders(MessageHeaders headers, String headerKey) {
    log.debug("Extracting header with name : {} from map : {}", headerKey, headers);
    if (headers.containsKey(headerKey)) {
      String result = (String) headers.get(headerKey);
      log.debug("Found header in the request with following key:value pair : {}:{}", headerKey,
          result);
      return result;
    }
    return null;
  }
}

