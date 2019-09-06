package com.osdu.model.osdu.delivery.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseItem {

    @JsonProperty("FileLocation")
    private String fileLocation;

    @JsonProperty("Data")
    private String data;

    @JsonProperty("SRN")
    private String srn;

}
