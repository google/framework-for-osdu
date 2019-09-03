package com.osdu.model.osdu.manifest;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class DeliveryManifestObject {

    @JsonAlias("Resources")
    private Resource[] resources;
}
