package com.osdu.model.osdu;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.osdu.model.SearchResult;
import lombok.Data;

import java.util.List;

@Data
public class OSDUSearchResult extends SearchResult {
    @JsonAlias("total_hits")
    private int totalHits;
    private List<String> facets;
    private int count;
    private int start;
}
