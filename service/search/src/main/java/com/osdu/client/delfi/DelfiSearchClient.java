package com.osdu.client.delfi;

import com.osdu.model.SearchObject;
import com.osdu.model.delfi.DelfiSearchResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(url = "${search.delfi.client.serverUrl}", name = "search.delfi")
public interface DelfiSearchClient {


  @PostMapping("${search.mapper.searchEndpoint}")
  DelfiSearchResult searchIndex(@RequestHeader(Header.AUTHORIZATION) String authToken,
      @RequestHeader(Header.APP_KEY) String applicationKey,
      @RequestHeader(Header.SLB_DATA_PARTITION_ID) String partition,
      SearchObject searchObject);
}
