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

package com.osdu.client;

import com.osdu.client.delfi.Header;
import com.osdu.model.delfi.signed.SignedUrlResult;
import com.osdu.model.delfi.status.JobStatusResponse;
import com.osdu.model.delfi.submit.SubmitFileObject;
import com.osdu.model.delfi.submit.SubmitFileResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(url = "${osdu.delfi.portal.url}/de/ingestion/v1", name = "delfi.ingestion.client")
public interface DelfiIngestionClient {

  @GetMapping("/landingzoneUrl?fileName={fileName}")
  SignedUrlResult getSignedUrlForLocation(@PathVariable("fileName") String fileName,
      @RequestHeader(Header.AUTHORIZATION) String authorizationToken,
      @RequestHeader(Header.APP_KEY) String applicationKey,
      @RequestHeader(Header.SLB_DATA_PARTITION_ID) String partition);

  @PostMapping("/submit")
  SubmitFileResult submitFile(@RequestHeader(Header.AUTHORIZATION) String authorization,
      @RequestHeader(Header.APP_KEY) String applicationKey,
      @RequestHeader(Header.SLB_DATA_PARTITION_ID) String partition,
      @RequestHeader(Header.SLB_ACCOUNT_ID) String accountId,
      SubmitFileObject submitFileObject);

  @GetMapping("/status?jobId={jobId}")
  JobStatusResponse getJobStatus(@PathVariable("jobId") String jobId,
      @RequestHeader(Header.AUTHORIZATION) String authorizationToken,
      @RequestHeader(Header.APP_KEY) String applicationKey,
      @RequestHeader(Header.SLB_DATA_PARTITION_ID) String partition);

}
