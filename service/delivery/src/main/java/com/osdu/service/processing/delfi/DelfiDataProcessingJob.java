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

import static com.osdu.config.AsyncConfiguration.DATA_PROCESSING_EXECUTOR;

import com.osdu.client.delfi.RecordDataFields;
import com.osdu.model.Record;
import com.osdu.model.SrnToRecord;
import com.osdu.model.delfi.DelfiFile;
import com.osdu.model.osdu.delivery.delfi.ProcessingResult;
import com.osdu.model.osdu.delivery.delfi.ProcessingResultStatus;
import com.osdu.service.PortalService;
import com.osdu.service.SrnMappingService;
import com.osdu.service.processing.DataProcessingJob;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DelfiDataProcessingJob implements DataProcessingJob {

  public static final String BUCKET_URL = "bucketURL";

  final SrnMappingService srnMappingService;
  final PortalService portalService;

  /**
   * Perform the async data processing for delivery for specified SRN.
   * It's run on {@code AsyncConfiguration#dataProcessingExecutor} executor.
   *
   * @param srn SRN
   * @param authorizationToken Bearer token
   * @param partition partition
   * @return {@link CompletableFuture} of delivery data processing result.
   */
  @Async(DATA_PROCESSING_EXECUTOR)
  public CompletableFuture<ProcessingResult> process(String srn, String authorizationToken,
      String partition) {

    ProcessingResult result = new ProcessingResult();
    result.setSrn(srn);

    SrnToRecord srnToRecord = srnMappingService.getSrnToRecord(srn);
    if (srnToRecord == null) {
      result.setProcessingResultStatus(ProcessingResultStatus.NO_MAPPING);
      return CompletableFuture.completedFuture(result);
    }
    String recordId = srnToRecord.getRecordId();
    final Record record = portalService.getRecord(recordId, authorizationToken, partition);
    if (record.getData().containsKey(BUCKET_URL)) {
      DelfiFile file = portalService
          .getFile(record.getData().get(BUCKET_URL).toString(), authorizationToken, partition);
      result.setProcessingResultStatus(ProcessingResultStatus.FILE);
      result.setData((Map<String, Object>) record.getData().get(RecordDataFields.OSDU_DATA));
      result.setFileLocation(file.getSignedUrl());
    } else {
      result.setData((Map<String, Object>) record.getData().get(RecordDataFields.OSDU_DATA));
      result.setProcessingResultStatus(ProcessingResultStatus.DATA);
    }
    return CompletableFuture.completedFuture(result);
  }
}