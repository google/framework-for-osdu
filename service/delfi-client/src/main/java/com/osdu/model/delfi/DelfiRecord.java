package com.osdu.model.delfi;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.osdu.model.Record;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DelfiRecord extends Record {

  String kind;

  @JsonProperty(access = Access.WRITE_ONLY)
  Long version;

  Acl acl;

  Legal legal;

  @JsonProperty(access = Access.WRITE_ONLY)
  String createUser;

  @JsonProperty(access = Access.WRITE_ONLY)
  LocalDateTime createTime;

  @JsonProperty(access = Access.WRITE_ONLY)
  String modifyUser;

  @JsonProperty(access = Access.WRITE_ONLY)
  LocalDateTime modifyTime;

}
