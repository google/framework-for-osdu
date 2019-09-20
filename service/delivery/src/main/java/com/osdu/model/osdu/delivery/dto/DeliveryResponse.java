package com.osdu.model.osdu.delivery.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class DeliveryResponse {

    private List<String> unprocessedSRNs;

    @JsonProperty("Result")
    private List<ResponseItem> result;

}
