package com.osdu.model.osdu;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.osdu.model.SearchObject;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class OSDUSearchObject extends SearchObject {
    private Integer count;
    private Integer start;
    private String fulltext;
    private Map<String, Object> metadata;
    private List<String> facets;
    @JsonAlias("full-results")
    private Boolean fullResults;
    private SortOption[] sort;
    @JsonAlias("geo-location")
    private GeoLocation geoLocation;
    @JsonAlias("geo-centroid")
    private List<Double>[] geoCentroid;
}
