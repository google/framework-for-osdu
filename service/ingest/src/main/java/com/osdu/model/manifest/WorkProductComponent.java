package com.osdu.model.manifest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.osdu.model.BaseJsonObject;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({ "files" })
public class WorkProductComponent extends BaseJsonObject {

  @JsonProperty("ResourceTypeID")
  String resourceTypeId;

  @JsonProperty(value = "Data")
  Map<String, Object> data;

  @JsonProperty("AssociativeID")
  String associativeId;

  @JsonProperty("FileAssociativeIDs")
  List<String> fileAssociativeIds;

  List<ManifestFile> files;

}
