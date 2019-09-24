package com.osdu.function;

import com.osdu.model.SearchResult;
import com.osdu.model.osdu.OsduSearchObject;
import com.osdu.service.SearchService;
import java.util.function.Function;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

/**
 * Function to Map OSDU compliant search query to Delfi query. Input format is described in
 * "SDU-82935841-250319-1033.pdf", output format is taken from API description from Delfi Developer
 * Portal -> Search Service -> /query
 */
@Component
@Slf4j
public class SearchServiceFunction implements
    Function<Message<OsduSearchObject>, Message<SearchResult>> {

  @Inject
  SearchService searchService;

  @Override
  public Message<SearchResult> apply(Message<OsduSearchObject> messageSource) {
    log.debug("Received request to search with following arguments: {}", messageSource);
    SearchResult searchResult = searchService
        .searchIndex(messageSource.getPayload(), messageSource.getHeaders());
    log.debug(
        "Result of the request to search with following arguments: {}, resulted in following object : {}",
        messageSource, searchResult);
    return new GenericMessage<>(searchResult);
  }
}
