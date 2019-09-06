package com.osdu.service.delfi;

import com.osdu.client.delfi.DelfiDeliveryClient;
import com.osdu.client.delfi.DelfiFileClient;
import com.osdu.client.delfi.DelfiOdesClient;
import com.osdu.exception.OSDUException;
import com.osdu.model.osdu.delivery.input.Srns;
import com.osdu.model.osdu.delivery.response.DeliveryResponse;
import com.osdu.service.DeliveryService;
import com.osdu.service.SRNMappingService;
import com.osdu.service.StorageService;
import com.osdu.service.processing.DataProcessingJob;
import com.osdu.service.processing.ProcessingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
public class DelfiDeliveryService implements DeliveryService {

    private final static Logger log = LoggerFactory.getLogger(DelfiDeliveryService.class);
    private static final int THREAD_POOL_CAPACITY = 3;

    @Inject
    private DelfiOdesClient delfiOdesClient;

    @Inject
    private StorageService storageService;

    @Inject
    private SRNMappingService srnMappingService;

    @Inject
    private DelfiDeliveryClient deliveryClient;

    @Inject
    private DelfiFileClient delfiFileClient;

    @Value("${osdu.download.resource.url}")
    private String downloadResourceLocation;

    @Override
    public DeliveryResponse getResources(Srns srns) {

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_CAPACITY);

        List<Future<ProcessingResult>> jobs = srns.getSrns()
                .stream().map(srn -> new DataProcessingJob(srn, delfiOdesClient, storageService))
                .map(job -> executor.submit(job))
                .collect(Collectors.toList());

        List<ProcessingResult> results = new ArrayList<>();
        for (Future<ProcessingResult> job : jobs) {
            try {
                results.add(job.get());
            } catch (ExecutionException | InterruptedException e) {
                log.error("Error execution srn", e);
                throw new OSDUException("Error execution srn");
            }
        }
        executor.shutdown();

        return processResults(results);
    }

    private DeliveryResponse processResults(List<ProcessingResult> results) {
        List<String> data = new ArrayList<>();
        List<String> unprocessedSrns = new ArrayList<>();
        results.stream().forEach(result -> {
            if (result.isProcessed()) {
                data.add(result.getData());
            } else {
                unprocessedSrns.add(result.getSrn());
            }
        });

        DeliveryResponse response = new DeliveryResponse();
        response.setData(data);
        response.setUnprocessedSRNs(unprocessedSrns);

        return response;
    }
}

