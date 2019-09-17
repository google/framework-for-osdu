package com.osdu.mapper;

import com.osdu.model.delfi.DelfiSearchResult;
import com.osdu.model.osdu.OsduSearchObject;
import com.osdu.model.osdu.OsduSearchResult;
import javax.inject.Inject;
import javax.inject.Named;

public abstract class OsduSearchResultMapperDecorator implements SearchResultMapper {

  @Inject
  @Named("com.osdu.mapper.SearchResultMapperImpl_")
  SearchResultMapper searchResultMapper;

  @Override
  public OsduSearchResult delfiSearchResultToOsduSearchResult(DelfiSearchResult searchResult,
      OsduSearchObject osduSearchObject) {
    OsduSearchResult osduSearchResult = searchResultMapper
        .delfiSearchResultToOsduSearchResult(searchResult, osduSearchObject);

    osduSearchResult.setFacets(osduSearchObject.getFacets());
    osduSearchResult.setCount(osduSearchObject.getCount());
    osduSearchResult.setStart(osduSearchObject.getStart());

    return osduSearchResult;
  }
}
