package com.osdu.mapper;

import com.osdu.model.delfi.DelfiSearchResult;
import com.osdu.model.osdu.OSDUSearchObject;
import com.osdu.model.osdu.OSDUSearchResult;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
@DecoratedWith(OSDUSearchResultMapperDecorator.class)
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
  OSDUSearchResult delfiSearchResultToOSDUSearchResult(DelfiSearchResult searchResult,
      OSDUSearchObject osduSearchObject);

}
