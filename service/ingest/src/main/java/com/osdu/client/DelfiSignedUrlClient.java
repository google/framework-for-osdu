package com.osdu.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(url = "${osdu.delfi.portal.url}", name = "delfi.delivery.file.client")
public interface DelfiSignedUrlClient {

  @GetMapping("/de/ingestion/v1/landingzoneUrl?fileName={fileName}")
  String getSignedUrlForLocation(@PathVariable("fileName") String fileName,
      @RequestHeader("Authorization") String authorizationToken,
      @RequestHeader("Slb-Account-Id") String accountId,
      @RequestHeader("Slb-Data-Partition-Id") String partition,
      @RequestHeader("Slb-On-Behalf-Of") String applicationKey);
}

