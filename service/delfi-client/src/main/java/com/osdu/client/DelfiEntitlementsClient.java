package com.osdu.client;

import static com.osdu.client.delfi.Header.APP_KEY;
import static com.osdu.client.delfi.Header.AUTHORIZATION;
import static com.osdu.client.delfi.Header.SLB_DATA_PARTITION_ID;

import com.osdu.model.delfi.entitlement.UserGroups;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(url = "${osdu.delfi.portal.url}/de/entitlements/v1",
    name = "delfi.entitlements.client")
public interface DelfiEntitlementsClient {

  @GetMapping("/groups")
  UserGroups getUserGroups(@RequestHeader(AUTHORIZATION) String authorizationToken,
      @RequestHeader(APP_KEY) String applicationKey,
      @RequestHeader(SLB_DATA_PARTITION_ID) String partition);

}
