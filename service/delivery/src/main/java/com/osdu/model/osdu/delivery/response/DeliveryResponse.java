package com.osdu.model.osdu.delivery.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URL;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
public class DeliveryResponse {

    private List<String> unprocessedSRNs;

    private List<String> data;

}
