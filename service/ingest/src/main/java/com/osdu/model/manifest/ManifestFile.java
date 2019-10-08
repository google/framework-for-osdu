package com.osdu.model.manifest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.osdu.model.BaseJsonObject;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties({ "wpc" })
@Builder(toBuilder = true)
public class ManifestFile extends BaseJsonObject {

  @JsonProperty("ResourceTypeID")
  String resourceTypeId;

  @JsonProperty(value = "Data")
  FileDataObject data;

  @JsonProperty("AssociativeID")
  String associativeId;

  WorkProductComponent wpc;

}
