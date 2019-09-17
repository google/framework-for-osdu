package com.osdu.model.osdu.delivery.manifest;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Data;

@Data
public class ManifestObject {

  @JsonAlias("DeliveryManifest")
  DeliveryManifestObject deliveryManifestObject;
}
