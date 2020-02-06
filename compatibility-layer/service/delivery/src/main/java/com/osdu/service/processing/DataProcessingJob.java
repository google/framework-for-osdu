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
