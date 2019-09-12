package com.osdu.service.processing;

import com.osdu.model.osdu.delivery.delfi.ProcessingResult;
import com.osdu.model.osdu.delivery.dto.DeliveryResponse;

import java.util.List;

public interface ResultDataConverter {
     DeliveryResponse convertProcessingResults(List<ProcessingResult> results);
}
