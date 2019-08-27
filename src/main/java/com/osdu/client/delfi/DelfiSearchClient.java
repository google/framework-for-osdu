package com.osdu.client.delfi;

import com.osdu.model.SearchObject;
import com.osdu.model.delfi.DelfiSearchResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(url = "${search.delfi.client.serverUrl}", name = "search.delfi")
public interface DelfiSearchClient {

    @PostMapping("${search.mapper.searchEndpoint}")
    DelfiSearchResult searchIndex(@RequestHeader("Authorization") String authToken,
                                         @RequestHeader("AppKey") String applicationKey,
                                         @RequestHeader("slb-data-partition-id") String partition,
                                         SearchObject searchObject);
}
