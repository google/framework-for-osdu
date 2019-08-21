package com.osdu.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.osdu.model.delfi.DelfiSearchObject;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;

/**
 * Delfi API query service
 */
@Service
public class DelfiSearchService implements SearchService {

    private final static Logger log = LoggerFactory.getLogger(DelfiSearchService.class);

    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String APP_KEY_HEADER = "AppKey";
    private static final String PARTITION_HEADER = "slb-data-partition-id";

    @Value("${search.mapper.delfi.url}")
    private String delfiPortalUrl;
    @Value("${search.mapper.searchEndpoint}")
    private String searchIndex;
    @Value("${search.mapper.delfi.appkey}")
    private String appKey;
    @Value("${search.mapper.delfi.partition}")
    private String partition;
    @Value("${search.mapper.delfi.appkey}")
    private String applicationKey;

    public String searchIndex(DelfiSearchObject delfiSearchObject, MessageHeaders headers) {
        OkHttpClient client = new OkHttpClient();
        ObjectMapper mapper = new ObjectMapper();
        RequestBody body = null;
        try {
            body = RequestBody.create(JSON, mapper.writeValueAsString(delfiSearchObject));
        } catch (Exception e) {
            log.debug("Error during creation of JSON object to send to Delfi", e);
        }
        Request request = new Request.Builder()
                .url(delfiPortalUrl + searchIndex)
                .header(AUTHORIZATION_HEADER, headers.get(AUTHORIZATION_HEADER.toLowerCase()).toString())
                .header(APP_KEY_HEADER, applicationKey)
                .header(PARTITION_HEADER, partition)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        } catch (Exception e) {
            log.debug("Error during querying of Delfi portal", e);
        }
        return null;
    }
}
