package com.osdu.service;

import com.osdu.client.delfi.DelfiSearchClient;
import com.osdu.model.SearchObject;
import com.osdu.model.SearchResult;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
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

    private static final String AUTHORIZATION_HEADER = "authorization";

    private final DelfiSearchClient delfiSearchClient;

    @Value("${search.mapper.delfi.partition}")
    private String partition;
    @Value("${search.mapper.delfi.appkey}")
    private String applicationKey;

    public DelfiSearchService(DelfiSearchClient delfiSearchClient) {
        this.delfiSearchClient = delfiSearchClient;
    }

    /**
     * NOT IMPLEMENTED YET
     * Searches Delfi partition using index
     *
     * @param searchObject parameters to use during search
     * @param headers      headers of the orriginal search request to get authorization header from them
     * @return {@link SearchResult} the result of the search from Delfi portal
     */
    @Override
    public SearchResult searchIndexWithCursor(SearchObject searchObject, MessageHeaders headers, String partitionOverride) {
        throw new NotImplementedException();
    }

    /**
     * Searches Delfi partition
     *
     * @param searchObject parameters to use during search
     * @param headers      headers of the orriginal search request to get authorization header from them
     * @return {@link SearchResult} the result of the search from Delfi portal
     */
    @Override
    public SearchResult searchIndex(SearchObject searchObject, MessageHeaders headers, String partitionOverride) {
        log.info("Received request to query Delfi Portal for data with following arguments: {},{}", searchObject, headers);

        SearchResult searchResult = delfiSearchClient.searchIndex(
                String.valueOf(headers.get(AUTHORIZATION_HEADER)),
                applicationKey,
                StringUtils.isEmpty(partitionOverride) ? partition : partitionOverride,
                searchObject);

        log.info("Received search result: {}", searchResult);
        return searchResult;
    }
}
