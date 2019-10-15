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
      SubmitFileObject submitFileObject);

  @GetMapping("/status?jobId={jobId}")
  JobStatusResponse getJobStatus(@PathVariable("jobId") String jobId,
      @RequestHeader(Header.AUTHORIZATION) String authorizationToken,
      @RequestHeader(Header.APP_KEY) String applicationKey,
      @RequestHeader(Header.SLB_DATA_PARTITION_ID) String partition);

}