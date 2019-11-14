package com.osdu.model.type.file;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.osdu.model.type.base.OsduObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class OsduFile extends OsduObject {

  @JsonIgnore
  FileData data;

  @Override
  @JsonProperty("Data")
  public FileData getData() {
    return data;
  }

}
