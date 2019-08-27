package com.osdu.service;

import com.osdu.model.SearchObject;
import com.osdu.model.SearchResult;

import org.springframework.messaging.MessageHeaders;

public interface SearchService {

    SearchResult searchIndexWithCursor(SearchObject searchObject, MessageHeaders headers, String partitionOverride);

    SearchResult searchIndex(SearchObject searchObject, MessageHeaders headers, String partitionOverride);

}
