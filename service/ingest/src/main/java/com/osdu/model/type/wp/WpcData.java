package com.osdu.model.type.wp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.osdu.model.type.base.OsduObjectData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class WpcData extends OsduObjectData {

  @JsonIgnore
  WpcGroupTypeProperties groupTypeProperties;

  @JsonIgnore
  WpcIndividualTypeProperties individualTypeProperties;

  @Override
  @JsonProperty("GroupTypeProperties")
  public WpcGroupTypeProperties getGroupTypeProperties() {
    return groupTypeProperties;
  }

  @Override
  @JsonProperty("IndividualTypeProperties")
  public WpcIndividualTypeProperties getIndividualTypeProperties() {
    return individualTypeProperties;
  }

}
