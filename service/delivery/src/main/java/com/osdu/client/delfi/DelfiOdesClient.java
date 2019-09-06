package com.osdu.client.delfi;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(url = "${}", name = "")
public interface DelfiOdesClient {

    default String getOdesId(String srn) {
        return srn;
    }
}
