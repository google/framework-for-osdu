package com.osdu.model.property;

import javax.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@ConfigurationProperties(prefix = "gcp.storage")
@Validated
@Component
public class CloudStorageProperties {

  @NotBlank
  String tempLocationBucket;

}
