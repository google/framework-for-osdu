package com.osdu.client.delfi;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(url = "${osdu.delfi.filePortal.url", name = "delfi.delivery.file.client")
public interface DelfiFileClient {

    @GetMapping("/_ah/api/signedUrlService/v1/sign?resourcePath={resourcePath}")
    public void getSignedUrlForLocation(@PathVariable("resourcePath") String resourcePath,
                                        @RequestHeader("Authorization") String authorizationToken,
                                        @RequestHeader("slb-data-partition-id") String partition,
                                        @RequestHeader("Slb-Account-Id") String accountId,
                                        @RequestHeader("AppKey") String applicationKey);
}

