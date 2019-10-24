package com.osdu.client;

import com.osdu.model.Record;
import com.osdu.model.delfi.DelfiRecord;
import com.osdu.model.delfi.SaveRecordsResult;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(url = "${osdu.delfi.portal.url}/de/storage/v2/records", name = "delfi.storage.client")
public interface DelfiStorageClient {

  @GetMapping("/{recordId}")
  DelfiRecord getRecord(@PathVariable("recordId") String recordId,
      @RequestHeader("Authorization") String authorizationToken,
      @RequestHeader("slb-data-partition-id") String partition,
      @RequestHeader("AppKey") String applicationKey);

  @PutMapping
  SaveRecordsResult putRecords(@RequestBody List<Record> records,
      @RequestHeader("Authorization") String authorizationToken,
      @RequestHeader("slb-data-partition-id") String partition,
      @RequestHeader("AppKey") String applicationKey);
}