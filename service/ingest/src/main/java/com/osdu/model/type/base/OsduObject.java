package com.osdu.model.type.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OsduObject {

  @JsonProperty("ResourceID")
  String resourceID;

  @JsonProperty("ResourceTypeID")
  String resourceTypeID;

  @JsonProperty("ResourceHomeRegionID")
  String resourceHomeRegionID;

  @JsonProperty("ResourceHostRegionIDs")
  List<String> resourceHostRegionIDs;

  @JsonProperty("ResourceObjectCreationDateTime")
  LocalDateTime resourceObjectCreationDatetime;

  @JsonProperty("ResourceVersionCreationDateTime")
  LocalDateTime resourceVersionCreationDatetime;

  @JsonProperty("ResourceCurationStatus")
  String resourceCurationStatus;

  @JsonProperty("ResourceLifecycleStatus")
  String resourceLifecycleStatus;

  @JsonProperty("ResourceSecurityClassification")
  String resourceSecurityClassification;

  @JsonIgnore
  OsduObjectData data;

  @JsonProperty("Data")
  public OsduObjectData getData() {
    return data;
  }

}
