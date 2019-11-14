package com.osdu.model.type.wp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.osdu.model.type.base.GroupTypeProperties;
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
public class WpcGroupTypeProperties extends GroupTypeProperties {

  @JsonProperty("Files")
  List<String> files;

  @JsonProperty("Artefacts")
  List<Artefact> artefacts;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Artefact {

    @JsonProperty("RoleID")
    String roleID;

    @JsonProperty("ResourceTypeID")
    String resourceTypeID;

    @JsonProperty("ResourceID")
    String resourceID;

  }

}
