/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.ingest.client;

import static org.opengroup.osdu.core.common.model.http.DpsHeaders.AUTHORIZATION;
import static org.opengroup.osdu.core.common.model.http.DpsHeaders.DATA_PARTITION_ID;

import org.opengroup.osdu.core.common.model.workflow.StartWorkflowRequest;
import org.opengroup.osdu.ingest.aspect.CheckClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(url = "${osdu.workflow-service.url}", name = "workflow-service")
public interface IWorkflowServiceClient {

  @CheckClientResponse
  @PostMapping("${osdu.workflow-service.start-workflow-endpoint}")
  feign.Response startWorkflow(@RequestHeader(AUTHORIZATION) String authToken,
      @RequestHeader(DATA_PARTITION_ID) String partition,
      @RequestBody StartWorkflowRequest startWorkflowRequest);
}
