package com.osdu.model.osdu;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.osdu.model.SearchResult;
import java.util.List;
import lombok.Data;

@Data
public class OSDUSearchResult extends SearchResult {

  @JsonAlias("total_hits")
  int totalHits;
  List<String> facets;
  int count;
  int start;
}
