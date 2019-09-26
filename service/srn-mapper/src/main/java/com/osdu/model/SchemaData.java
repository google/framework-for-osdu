package com.osdu.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class SchemaData {

  String srn;
  String kind;
  JsonNode schema;
}
