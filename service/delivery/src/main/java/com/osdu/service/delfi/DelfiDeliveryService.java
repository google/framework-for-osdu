package com.osdu.service.delfi;

import static com.osdu.request.OsduHeader.extractHeaderByName;

import com.osdu.exception.OsduException;
import com.osdu.model.osdu.delivery.delfi.ProcessingResult;
import com.osdu.model.osdu.delivery.dto.DeliveryResponse;
import com.osdu.model.osdu.delivery.input.InputPayload;
import com.osdu.model.osdu.delivery.property.OsduDeliveryProperties;
import com.osdu.service.AuthenticationService;
import com.osdu.service.DeliveryService;
import com.osdu.service.PortalService;
import com.osdu.service.SrnMappingService;
import com.osdu.service.processing.ResultDataConverter;
import com.osdu.service.processing.delfi.DelfiDataProcessingJob;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

  public static final String PARTITION_HEADER_KEY = "partition";
  public static final String AUTHORIZATION_HEADER_KEY = "authorization";

  final SrnMappingService srnMappingService;
  final PortalService portalService;
  final ResultDataConverter resultDataConverter;
  final AuthenticationService authenticationService;
  final OsduDeliveryProperties properties;

  @SuppressWarnings("unchecked")
  @Override
  public DeliveryResponse getResources(InputPayload inputPayload, MessageHeaders headers) {
    log.debug("Getting resources for following SRNs and headers : {}, {}", inputPayload, headers);
    ExecutorService executor = Executors.newFixedThreadPool(properties.getThreadPoolCapacity());

    String authorizationToken = extractHeaderByName(headers, AUTHORIZATION_HEADER_KEY);
    String partition = extractHeaderByName(headers, PARTITION_HEADER_KEY);

    authenticationService.checkAuthentication(authorizationToken, partition);

    Map<String, Future<ProcessingResult>> srnToFutureMap = inputPayload.getSrns().stream()
        .collect(Collectors.toMap(Function.identity(), srn -> {
          DelfiDataProcessingJob delfiDataProcessingJob = new DelfiDataProcessingJob(srn,
              srnMappingService, portalService,
              authorizationToken, partition);
          return executor.submit(delfiDataProcessingJob);
        }));

    List<ProcessingResult> results = srnToFutureMap.entrySet().stream().map(srnToFuture -> {
      Future<ProcessingResult> job = srnToFuture.getValue();
      try {
        return job.get();
      } catch (ExecutionException e) {
        String message = "Error execution srn - " + srnToFuture.getKey();
        log.error(message, e);
        throw new OsduException(message, e);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        String message = "Error execution srn - " + srnToFuture.getKey();
        log.error(message, e);
        throw new OsduException(message);
      }
    }).collect(Collectors.toList());

    executor.shutdown();

    return resultDataConverter.convertProcessingResults(results);
  }

}

