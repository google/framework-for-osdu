package com.osdu.client;

import static com.osdu.client.delfi.Header.APP_KEY;
import static com.osdu.client.delfi.Header.AUTHORIZATION;
import static com.osdu.client.delfi.Header.SLB_DATA_PARTITION_ID;

import com.osdu.model.delfi.DelfiFile;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(url = "${osdu.delfi.filePortal.url}", name = "delfi.delivery.file.client")
public interface DelfiFileClient {

  @GetMapping("/_ah/api/signedUrlService/v1/sign?resourcePath={resourcePath}")
  DelfiFile getSignedUrlForLocation(@PathVariable("resourcePath") String resourcePath,
      @RequestHeader(AUTHORIZATION) String authorizationToken,
      @RequestHeader(APP_KEY) String applicationKey,
      @RequestHeader(SLB_DATA_PARTITION_ID) String partition);
}

