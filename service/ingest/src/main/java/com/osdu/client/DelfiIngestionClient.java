package com.osdu.client;

import com.osdu.client.delfi.Header;
import com.osdu.model.delfi.JobStatus;
import com.osdu.model.delfi.SignedUrlResult;
import com.osdu.model.delfi.SubmitFileObject;
import com.osdu.model.delfi.SubmitFileResult;
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
      @RequestHeader(Header.SLB_ACCOUNT_ID) String accountId,
      @RequestHeader(Header.SLB_DATA_PARTITION_ID) String partition);

  @PostMapping("/submit")
  SubmitFileResult submitFile(@RequestHeader(Header.AUTHORIZATION) String authorization,
      @RequestHeader(Header.APP_KEY) String applicationKey,
      @RequestHeader(Header.SLB_ACCOUNT_ID) String accountId,
      @RequestHeader(Header.SLB_DATA_PARTITION_ID) String partition,
      SubmitFileObject submitFileObject);

  @GetMapping("/status?jobId={jobId}")
  JobStatus getJobStatus(@PathVariable("jobId") String jobId,
      @RequestHeader(Header.AUTHORIZATION) String authorizationToken,
      @RequestHeader(Header.APP_KEY) String applicationKey,
      @RequestHeader(Header.SLB_ACCOUNT_ID) String accountId,
      @RequestHeader(Header.SLB_DATA_PARTITION_ID) String partition);

}
