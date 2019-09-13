package com.osdu.model.osdu;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.osdu.model.SearchObject;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class OSDUSearchObject extends SearchObject {

  Integer count;
  Integer start;
  String fulltext;
  Map<String, Object> metadata;
  List<String> facets;
  @JsonAlias("full-results")
  Boolean fullResults;
  SortOption[] sort;
  @JsonAlias("geo-location")
  GeoLocation geoLocation;
  @JsonAlias("geo-centroid")
  List<Double>[] geoCentroid;
}
