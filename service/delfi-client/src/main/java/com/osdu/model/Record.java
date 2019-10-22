package com.osdu.model;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Record extends BaseJsonObject implements BaseRecord {

  String id;

  Map<String, Object> data = new HashMap<>();

}
