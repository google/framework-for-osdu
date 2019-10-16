package com.osdu.client;

import com.osdu.client.delfi.Header;
import com.osdu.model.delfi.entitlement.UserGroups;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(url = "${osdu.delfi.portal.url}/de/entitlements/v1",
    name = "delfi.entitlements.client")
public interface DelfiEntitlementsClient {

  @GetMapping("/groups")
  UserGroups getUserGroups(@RequestHeader(Header.AUTHORIZATION) String authorizationToken,
      @RequestHeader(Header.APP_KEY) String applicationKey,
      @RequestHeader(Header.SLB_DATA_PARTITION_ID) String partition);

}
