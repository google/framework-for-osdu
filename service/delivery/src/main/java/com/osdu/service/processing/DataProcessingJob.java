package com.osdu.service.processing;

import com.osdu.model.osdu.delivery.delfi.ProcessingResult;
import java.util.concurrent.CompletableFuture;

public interface DataProcessingJob {

  /**
   * Perform the data processing for delivery for specified SRN.
   *
   * @param srn SRN
   * @param authorizationToken Bearer token
   * @param partition partition
   * @return {@link CompletableFuture} of delivery data processing result.
   */
  CompletableFuture<ProcessingResult> process(String srn, String authorizationToken,
      String partition);

}
