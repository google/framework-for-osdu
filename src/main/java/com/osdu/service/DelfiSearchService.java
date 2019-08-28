package com.osdu.service;

import com.osdu.client.delfi.DelfiSearchClient;
import com.osdu.mapper.SearchObjectMapper;
import com.osdu.mapper.SearchResultMapper;
import com.osdu.model.SearchObject;
import com.osdu.model.SearchResult;
import com.osdu.model.delfi.DelfiSearchObject;
import com.osdu.model.delfi.DelfiSearchResult;
import com.osdu.model.delfi.geo.exception.GeoLocationException;
import com.osdu.model.osdu.OSDUSearchObject;
import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Delfi API query service
 */
@Service
public class DelfiSearchService implements SearchService {

    private final static Logger log = LoggerFactory.getLogger(DelfiSearchService.class);

    private static final String KIND_HEADER_KEY = "kind";
    private static final String PARTITION_HEADER_KEY = "partition";
    private static final String AUTHORIZATION_HEADER = "authorization";

    private final DelfiSearchClient delfiSearchClient;

    @Inject
    @Named
    private SearchObjectMapper searchObjectMapper;

    @Inject
    @Named
    private SearchResultMapper searchResultMapper;

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
    public SearchResult searchIndexWithCursor(SearchObject searchObject, MessageHeaders headers) {
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
    public SearchResult searchIndex(SearchObject searchObject, MessageHeaders headers) throws GeoLocationException {
        log.info("Received request to query Delfi Portal for data with following arguments: {},{}", searchObject, headers);

        String kind = extractHeaders(headers, KIND_HEADER_KEY);
        String partition = extractHeaders(headers, PARTITION_HEADER_KEY);

        DelfiSearchObject delfiSearchObject = searchObjectMapper.osduSearchObjectToDelfiSearchObject((OSDUSearchObject) searchObject, kind, partition);
        DelfiSearchResult searchResult = delfiSearchClient.searchIndex(
                String.valueOf(headers.get(AUTHORIZATION_HEADER)),
                applicationKey,
                partition,
                delfiSearchObject);
        SearchResult osduSearchResult = searchResultMapper.delfiSearchResultToOSDUSearchResult(searchResult, (OSDUSearchObject) searchObject);
        log.info("Received search result: {}", osduSearchResult);
        return osduSearchResult;
    }

    private String extractHeaders(MessageHeaders headers, String headerKey) {
        if (headers.containsKey(headerKey)) {
            String result = (String) headers.get(headerKey);
            log.debug("Found {} override in the request, using following parameter: {}", headerKey, result);
            return result;
        }
        return null;
    }
}
