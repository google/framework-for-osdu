package com.osdu.client;

import com.osdu.model.Record;
import com.osdu.model.delfi.DelfiRecord;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(url = "${osdu.delfi.portal.url}", name = "delfi.delivery.client")
public interface DelfiDeliveryClient {

  @GetMapping("/de/storage/v2/records/{recordId}")
  DelfiRecord getRecord(@PathVariable("recordId") String recordId,
      @RequestHeader("Authorization") String authorizationToken,
      @RequestHeader("slb-data-partition-id") String partition,
      @RequestHeader("AppKey") String applicationKey);

  @PutMapping("/de/storage/v2/records")
  DelfiRecord putRecord(@RequestBody Record record,
      @RequestHeader("Authorization") String authorizationToken,
      @RequestHeader("slb-data-partition-id") String partition,
      @RequestHeader("AppKey") String applicationKey);
}