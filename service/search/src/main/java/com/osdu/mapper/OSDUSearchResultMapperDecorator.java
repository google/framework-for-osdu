package com.osdu.mapper;

import com.osdu.model.delfi.DelfiSearchResult;
import com.osdu.model.osdu.OSDUSearchObject;
import com.osdu.model.osdu.OSDUSearchResult;
import javax.inject.Inject;
import javax.inject.Named;

public abstract class OSDUSearchResultMapperDecorator implements SearchResultMapper {

  @Inject
  @Named("com.osdu.mapper.SearchResultMapperImpl_")
  SearchResultMapper searchResultMapper;

  @Override
  public OSDUSearchResult delfiSearchResultToOSDUSearchResult(DelfiSearchResult searchResult,
      OSDUSearchObject osduSearchObject) {
    OSDUSearchResult osduSearchResult = searchResultMapper
        .delfiSearchResultToOSDUSearchResult(searchResult, osduSearchObject);

    osduSearchResult.setFacets(osduSearchObject.getFacets());
    osduSearchResult.setCount(osduSearchObject.getCount());
    osduSearchResult.setStart(osduSearchObject.getStart());

    return osduSearchResult;
  }
}
