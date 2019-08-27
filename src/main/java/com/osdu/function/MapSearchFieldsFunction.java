package com.osdu.function;

import com.osdu.exception.OSDUException;
import com.osdu.mapper.SearchObjectMapper;
import com.osdu.mapper.SearchResultMapper;
import com.osdu.model.SearchResult;
import com.osdu.model.delfi.DelfiSearchObject;
import com.osdu.model.delfi.DelfiSearchResult;
import com.osdu.model.osdu.OSDUSearchObject;
import com.osdu.service.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.function.Function;

/**
 * Function to Map OSDU compliant search query to Delfi query.
 * Input format is described in "SDU-82935841-250319-1033.pdf", output format is taken from API description from
 * Delfi Developer Portal -> Search Service -> /query
 */
@Component
public class MapSearchFieldsFunction implements Function<Message<OSDUSearchObject>, Message<SearchResult>> {

    private final static Logger log = LoggerFactory.getLogger(MapSearchFieldsFunction.class);
    public static final String KIND_HEADER_KEY = "kind";
    public static final String PARTITION_HEADER_KEY = "partition";


    @Value("${search.mapper.delfi.partition}")
    private String partition;

    @Autowired
    private SearchService delfiSearchService;

    @Autowired
    private SearchObjectMapper searchObjectMapper;

    @Autowired
    private SearchResultMapper searchResultMapper;

    @Override
    public Message<SearchResult> apply(Message<OSDUSearchObject> messageSource) {
        try {
            log.info("Received request to search with following arguments: {}", messageSource);
            OSDUSearchObject payload = messageSource.getPayload();
            MessageHeaders headers = messageSource.getHeaders();
            String partition = null;
            String kind = null;

            //Code below was agreed to use as a temporary solution - there is a header policy
            //in Apigee that let's you use change those parameters. They are removed
            //from requests coming from outside and are here only while we have problems with solid test
            // partitions and kinds.
            if (headers.containsKey(KIND_HEADER_KEY)) {
                kind = (String) headers.get(KIND_HEADER_KEY);
                log.debug("Found kind override in the request, using following kind : {}", kind);
            }
            if (headers.containsKey(PARTITION_HEADER_KEY)) {
                partition = (String) headers.get(PARTITION_HEADER_KEY);
                log.debug("Found partition override in the request, using following parition : {}", partition);
            }

            DelfiSearchObject delfiSearchObject = searchObjectMapper.osduSearchObjectToDelfiSearchObject(payload, kind, partition);
            SearchResult delfiSearchResult = delfiSearchService.searchIndex(delfiSearchObject, messageSource.getHeaders(), partition);
            SearchResult osduSearchResult = searchResultMapper.delfiSearchResultToOSDUSearchResult((DelfiSearchResult) delfiSearchResult, payload);
            log.info("Result of the request to search with following arguments: {}, resulted in following object : {}", messageSource, osduSearchResult);
            return new GenericMessage<>(osduSearchResult, Collections.singletonMap("Content-Type", "application/json;charset=UTF-8"));
        } catch (OSDUException e) {
            log.error("Failed to serve request to search", e);
            throw new RuntimeException(e.getMessage());
        }
    }
}
