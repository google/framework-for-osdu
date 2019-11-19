package com.osdu.model.type.wp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.osdu.model.type.base.IndividualTypeProperties;
import com.osdu.model.type.reference.SpatialLocation;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class WpcIndividualTypeProperties extends IndividualTypeProperties {

  @JsonProperty("Name")
  String name;

  @JsonProperty("Description")
  String description;

  @JsonProperty("CreationDateTime")
  LocalDateTime creationDateTime;

  @JsonProperty("Tags")
  List<String> tags;

  @JsonProperty("SpatialPoint")
  SpatialLocation spatialPoint;

  @JsonProperty("SpatialArea")
  SpatialLocation spatialArea;

  @JsonProperty("SubmitterName")
  String submitterName;

  @JsonProperty("BusinessActivites")
  List<String> businessActivites;

  @JsonProperty("AuthorIDs")
  List<String> authorIDs;

  @JsonProperty("LineageAssertions")
  List<LineageAssertion> lineageAssertions;

  @JsonProperty("Annotations")
  List<String> annotations;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class LineageAssertion {

    @JsonProperty("ID")
    String id;

    @JsonProperty("RelationshipType")
    LineageAssertion.RelationshipType relationshipType;

    public enum RelationshipType {
      PREDECESSOR, SOURCE, REFERENCE
    }
  }

}
