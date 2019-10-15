package com.osdu.model.osdu;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.osdu.deserializer.MetadataDeserializer;
import com.osdu.model.SearchObject;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class OsduSearchObject implements SearchObject {

  Integer count;
  Integer start;
  String fulltext;
  @JsonDeserialize(using = MetadataDeserializer.class)
  Map<String, List<String>> metadata;
  List<String> facets;
  @JsonAlias("full-results")
  Boolean fullResults;
  SortOption[] sort;
  @JsonAlias("geo-location")
  GeoLocation geoLocation;
  @JsonAlias("geo-centroid")
  List<Double>[] geoCentroid;
}
