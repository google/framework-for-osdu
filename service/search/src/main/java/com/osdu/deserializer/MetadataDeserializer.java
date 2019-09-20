package com.osdu.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.osdu.exception.SearchException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;

/**
 * Since the actual metadata can come in different formats depending on the number of values in it
 * it was decided that a more efficient approach would be to modify the object at deserialization.
 * Basically we are converting a map of <String,String or List<String>> which would have to be
 * replaced with <String,Object> to <String,List<String>
 */
@Slf4j
public class MetadataDeserializer extends JsonDeserializer<Map<String, List<String>>> {

  @Override
  public Map<String, List<String>> deserialize(JsonParser jsonParser,
      DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
    log.debug("Deserializing metadata : jsonParser : {}, context : {}", jsonParser,
        deserializationContext);
    JsonNode node = jsonParser.getCodec().readTree(jsonParser);
    if (!node.fields().hasNext()) {
      return null;
    }

    ObjectReader reader = new ObjectMapper().reader().forType(new TypeReference<List<String>>() {
    });
    Iterable<Entry<String, JsonNode>> iterable = node::fields;
    final Map<String, List<String>> collect = StreamSupport.stream(iterable.spliterator(), false)
        .collect(Collectors
            .toMap(Entry::getKey,
                metadataEntry -> {
                  try {
                    return (metadataEntry.getValue() instanceof ArrayNode)
                        ? reader.readValue(metadataEntry.getValue())
                        : Collections.singletonList(metadataEntry.getValue().asText());
                  } catch (IOException e) {
                    throw new SearchException(String
                        .format("Failed to map metadata : %s, context : %s", jsonParser,
                            deserializationContext), e);
                  }
                }));
    log.debug("Result of deserialization of metadata : {} ", collect);
    return collect;
  }
}
