package com.osdu.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.osdu.model.osdu.SortOption;
import java.io.IOException;
import java.util.Map;

/**
 * Custom deserializer for OSDU search object. Needed since the structure defined in documentation
 * describes two different styles for inner objects of the sort array - 1 - Object with inner field
 * named as a fieldName and inner object of that field with K:V pair order:orderType (example : {
 * "region":{ "order":"asc" } } ) 2 - String with the name of the field. (example : "fieldName")
 */
public class SortOptionDeserializer extends JsonDeserializer<SortOption> {

  private static final String ORDER_JSON_KEY = "order";

  @Override
  public SortOption deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    JsonNode node = p.getCodec().readTree(p);
    SortOption sortOption = new SortOption();

    if (node.fields().hasNext()) {
      Map.Entry<String, JsonNode> sortEntry = node.fields().next();
      sortOption.setFieldName(sortEntry.getKey());
      sortOption
          .setOrderType(
              SortOption.OrderType.valueOf(sortEntry.getValue().get(ORDER_JSON_KEY).asText()));
    } else {
      sortOption.setFieldName(node.asText());
      sortOption.setOrderType(SortOption.OrderType.ASC);
    }

    return sortOption;
  }
}
