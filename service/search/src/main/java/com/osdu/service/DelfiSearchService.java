/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.osdu.service;

import static com.osdu.request.OsduHeader.extractHeaderByName;
import static java.util.Objects.isNull;

import com.osdu.client.delfi.DelfiSearchClient;
import com.osdu.exception.SearchException;
import com.osdu.mapper.SearchObjectMapper;
import com.osdu.mapper.SearchResultMapper;
import com.osdu.model.SearchObject;
import com.osdu.model.SearchResult;
import com.osdu.model.delfi.DelfiSearchObject;
import com.osdu.model.delfi.DelfiSearchResult;
import com.osdu.model.osdu.OsduSearchObject;
import com.osdu.model.property.DelfiPortalProperties;
import com.osdu.request.OsduHeader;
import javax.inject.Named;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;

/**
 * Delfi API query service.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DelfiSearchService implements SearchService {

  static final String KIND_HEADER_KEY = "kind";

  final DelfiPortalProperties portalProperties;

  @Named
  final SearchObjectMapper searchObjectMapper;
  @Named
  final SearchResultMapper searchResultMapper;

  final DelfiSearchClient delfiSearchClient;
  final AuthenticationService authenticationService;

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
    String partition = extractHeaderByName(headers, OsduHeader.PARTITION);
    String authorizationToken = extractHeaderByName(headers, OsduHeader.AUTHORIZATION);

    authenticationService.getUserGroups(authorizationToken, partition);

    checkIfInputParametersValid((OsduSearchObject) searchObject);

    DelfiSearchObject delfiSearchObject = searchObjectMapper
        .osduToDelfi((OsduSearchObject) searchObject, kind, partition);
    DelfiSearchResult searchResult = delfiSearchClient.searchIndex(
        authorizationToken,
        portalProperties.getAppKey(),
        partition,
        delfiSearchObject);
    SearchResult osduSearchResult = searchResultMapper
        .delfiToOsdu(searchResult, (OsduSearchObject) searchObject);
    log.debug("Received search result: {}", osduSearchResult);
    return osduSearchResult;
  }

  private void checkIfInputParametersValid(OsduSearchObject searchObject) {
    if (isNull(searchObject.getFulltext())
        && isNull(searchObject.getMetadata())
        && isNull(searchObject.getGeoCentroid())
        && isNull(searchObject.getGeoLocation())) {
      throw new SearchException("Input parameters validation fail - " + searchObject);
    }
  }
}
