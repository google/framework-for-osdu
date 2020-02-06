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

package com.osdu.mapper;

import com.osdu.model.delfi.DelfiSearchResult;
import com.osdu.model.osdu.OsduSearchObject;
import com.osdu.model.osdu.OsduSearchResult;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
@DecoratedWith(SearchResultMapperDecorator.class)
public interface SearchResultMapper {

  /**
   * Maps {@link DelfiSearchResult} to OSDUSearchResult.
   *
   * @param searchResult     to get actual search result data
   * @param osduSearchObject to get additional metedata information to enrich the result. OSDU
   *                         result includes some properties of the original search request like
   *                         facets or requested count/offset.
   * @return result of the search against Delfi Portal in OSDU compliant format.
   */
  @Mapping(source = "searchResult.totalCount", target = "totalHits")
  @Mapping(target = "start", ignore = true)
  OsduSearchResult delfiToOsdu(DelfiSearchResult searchResult,
      OsduSearchObject osduSearchObject);

}
