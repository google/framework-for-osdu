package com.osdu.model.osdu.delivery;

import lombok.Data;

import java.util.Map;

@Data
public abstract class Record {
    Map<String, Object> data;
}
