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
import javax.inject.Inject;
import javax.inject.Named;

public abstract class SearchResultMapperDecorator implements SearchResultMapper {

  @Inject
  @Named("com.osdu.mapper.SearchResultMapperImpl_")
  SearchResultMapper searchResultMapper;

  @Override
  public OsduSearchResult delfiToOsdu(DelfiSearchResult searchResult,
      OsduSearchObject osduSearchObject) {
    OsduSearchResult osduSearchResult = searchResultMapper
        .delfiToOsdu(searchResult, osduSearchObject);

    osduSearchResult.setFacets(osduSearchObject.getFacets());
    osduSearchResult.setCount(osduSearchObject.getCount());
    osduSearchResult.setStart(osduSearchObject.getStart());

    return osduSearchResult;
  }
}
