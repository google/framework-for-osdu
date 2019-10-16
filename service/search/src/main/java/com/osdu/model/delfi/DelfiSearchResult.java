package com.osdu.model.delfi;

import com.osdu.model.SearchResult;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DelfiSearchResult extends SearchResult {

  Integer totalCount;

}
