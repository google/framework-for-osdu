package com.osdu.service;

import com.osdu.model.SearchObject;
import com.osdu.model.SearchResult;
import com.osdu.model.delfi.geo.exception.GeoLocationException;
import org.springframework.messaging.MessageHeaders;

public interface SearchService {

  SearchResult searchIndexWithCursor(SearchObject searchObject, MessageHeaders headers);

  SearchResult searchIndex(SearchObject searchObject, MessageHeaders headers)
      throws GeoLocationException;

}
