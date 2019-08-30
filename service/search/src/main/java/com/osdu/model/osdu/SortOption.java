package com.osdu.model.osdu;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.osdu.deserializer.SortOptionDeserializer;
import lombok.Data;

@Data
@JsonDeserialize(using = SortOptionDeserializer.class)
public class SortOption {

    public enum OrderType{
        asc,desc
    }

    private String fieldName;
    private OrderType orderType;

}
