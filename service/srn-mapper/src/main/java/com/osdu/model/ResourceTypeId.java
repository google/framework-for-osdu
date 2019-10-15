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
