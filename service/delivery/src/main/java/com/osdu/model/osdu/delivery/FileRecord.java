package com.osdu.model.osdu.delivery;

import lombok.Data;

import java.util.Map;

@Data
public abstract class FileRecord {
    Map<String,Object> data;
}
