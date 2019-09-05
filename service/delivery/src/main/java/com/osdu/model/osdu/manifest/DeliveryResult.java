package com.osdu.model.osdu.manifest;

import lombok.Builder;
import lombok.Data;

import java.net.URL;

@Data
@Builder
public class DeliveryResult {

    private URL resourceLink;

}
