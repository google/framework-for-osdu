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

package com.osdu.function;

import com.osdu.model.SearchResult;
import com.osdu.model.osdu.OsduSearchObject;
import com.osdu.service.SearchService;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
public class SearchServiceFunction implements
    Function<Message<OsduSearchObject>, Message<SearchResult>> {

  final SearchService searchService;

  @Override
  public Message<SearchResult> apply(Message<OsduSearchObject> messageSource) {
    log.debug("Received request to search with following arguments: {}", messageSource);
    SearchResult searchResult = searchService
        .searchIndex(messageSource.getPayload(), messageSource.getHeaders());
    log.debug("Result of the request to search with following arguments: {}, "
            + "resulted in following object : {}", messageSource, searchResult);
    return new GenericMessage<>(searchResult);
  }
}
