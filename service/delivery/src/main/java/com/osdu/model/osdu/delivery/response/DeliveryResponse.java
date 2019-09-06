package com.osdu.model.osdu.delivery.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class DeliveryResponse {

    private List<String> unprocessedSRNs;

    private List<String> data;

}
