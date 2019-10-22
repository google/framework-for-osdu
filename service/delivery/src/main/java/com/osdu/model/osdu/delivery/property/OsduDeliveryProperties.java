package com.osdu.model.osdu.delivery.property;

import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@ConfigurationProperties(prefix = "osdu.processing")
@Validated
@Component
public class OsduDeliveryProperties {

  List<String> fieldsToStrip;

  @NotNull
  Integer threadPoolCapacity;

}
