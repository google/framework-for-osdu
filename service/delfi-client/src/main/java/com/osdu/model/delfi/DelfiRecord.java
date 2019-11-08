/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.osdu.model.delfi;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.osdu.model.Record;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
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

  @Builder
  public DelfiRecord(Map<String, Object> data, String id, String kind,
      Long version, Acl acl, Legal legal, String createUser, LocalDateTime createTime,
      String modifyUser, LocalDateTime modifyTime) {
    super(data, id);
    this.kind = kind;
    this.version = version;
    this.acl = acl;
    this.legal = legal;
    this.createUser = createUser;
    this.createTime = createTime;
    this.modifyUser = modifyUser;
    this.modifyTime = modifyTime;
  }

}
