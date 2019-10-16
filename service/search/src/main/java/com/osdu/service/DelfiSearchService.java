package com.osdu.service;

import static com.osdu.request.OsduHeader.extractHeaderByName;
import static java.util.Objects.isNull;

import com.osdu.client.delfi.DelfiSearchClient;
import com.osdu.mapper.SearchObjectMapper;
import com.osdu.mapper.SearchResultMapper;
import com.osdu.model.SearchObject;
import com.osdu.model.SearchResult;
import com.osdu.model.delfi.DelfiSearchObject;
import com.osdu.model.delfi.DelfiSearchResult;
import com.osdu.model.osdu.OsduSearchObject;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;

/**
 * Delfi API query service.
 */
@Service
@Slf4j
public class DelfiSearchService implements SearchService {

  static final String KIND_HEADER_KEY = "kind";
  static final String PARTITION_HEADER_KEY = "partition";
  static final String AUTHORIZATION_HEADER = "authorization";

  final DelfiSearchClient delfiSearchClient;

  @Inject
  @Named
  SearchObjectMapper searchObjectMapper;

  @Inject
  @Named
  SearchResultMapper searchResultMapper;

  @Inject
  AuthenticationService authenticationService;

  @Value("${search.mapper.delfi.appkey}")
  String applicationKey;

  public DelfiSearchService(DelfiSearchClient delfiSearchClient) {
    this.delfiSearchClient = delfiSearchClient;
  }

  /**
   * NOT IMPLEMENTED YET Searches Delfi partition using index.
   *
   * @param searchObject parameters to use during search
   * @param headers      headers of the orriginal search request to get authorization header from
   *                     them
   * @return {@link SearchResult} the result of the search from Delfi portal
   */
  @Override
  public SearchResult searchIndexWithCursor(SearchObject searchObject, MessageHeaders headers) {
    throw new NotImplementedException();
  }

  /**
   * Searches Delfi partition.
   *
   * @param searchObject parameters to use during search
   * @param headers      headers of the orriginal search request to get authorization header from
   *                     them
   * @return {@link SearchResult} the result of the search from Delfi portal
   */
  @Override
  public SearchResult searchIndex(SearchObject searchObject, MessageHeaders headers) {
    log.debug("Received request to query Delfi Portal for data with following arguments: {},{}",
        searchObject, headers);

    String kind = extractHeaderByName(headers, KIND_HEADER_KEY);
    String partition = extractHeaderByName(headers, PARTITION_HEADER_KEY);
    String authorizationToken = extractHeaderByName(headers, AUTHORIZATION_HEADER);

    authenticationService.checkAuthentication(authorizationToken, partition);

    Boolean valid = checkIfInputParametersValid((OsduSearchObject) searchObject);
    if (Boolean.FALSE.equals(valid)) {
      log.info("Input parameters validation fail - " + (OsduSearchObject) searchObject);
      return new SearchResult();
    }

    DelfiSearchObject delfiSearchObject = searchObjectMapper
        .osduToDelfi((OsduSearchObject) searchObject, kind, partition);
    DelfiSearchResult searchResult = delfiSearchClient.searchIndex(
        authorizationToken,
        applicationKey,
        partition,
        delfiSearchObject);
    SearchResult osduSearchResult = searchResultMapper
        .delfiToOsdu(searchResult, (OsduSearchObject) searchObject);
    log.debug("Received search result: {}", osduSearchResult);
    return osduSearchResult;
  }

  private Boolean checkIfInputParametersValid(OsduSearchObject searchObject) {
    return !(isNull(searchObject.getFulltext())
        && isNull(searchObject.getMetadata())
        && isNull(searchObject.getGeoCentroid())
        && isNull(searchObject.getGeoLocation()));
  }
}
