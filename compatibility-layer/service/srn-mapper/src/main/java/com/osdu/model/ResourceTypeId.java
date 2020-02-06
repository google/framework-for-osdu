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

package com.osdu.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class ResourceTypeId {

  private static final Pattern PATTERN = Pattern.compile("^srn:type:(.*):(.*)$");

  private String raw;
  private String type;
  private String version;
  private ResourceType resourceType;

  /**
   * Constructor for ResourceTypeId.
   *
   * @param resourceTypeId type id
   */
  public ResourceTypeId(String resourceTypeId) {
    this.raw = resourceTypeId;

    Matcher matcher = PATTERN.matcher(resourceTypeId);
    if (matcher.find()) {
      this.type = matcher.group(1);
      this.version = matcher.group(2);
      this.resourceType = ResourceType.fromType(type);
    }
  }

  public boolean hasVersion() {
    return StringUtils.isNotBlank(version);
  }

}
