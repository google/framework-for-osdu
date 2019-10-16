package com.osdu.model.osdu;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.osdu.model.SearchResult;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OsduSearchResult extends SearchResult {

  @JsonAlias("total_hits")
  int totalHits;
  List<String> facets;
  int count;
  int start;
}
