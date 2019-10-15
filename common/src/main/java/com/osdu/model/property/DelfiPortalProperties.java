package com.osdu.model.property;

import javax.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@ConfigurationProperties(prefix = "osdu.delfi.portal")
@Validated
@Component
public class DelfiPortalProperties {

  @NotBlank
  String appKey;

}
