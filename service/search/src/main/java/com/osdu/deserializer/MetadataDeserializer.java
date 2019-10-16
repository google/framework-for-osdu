package com.osdu.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.osdu.model.deserializer.MetadataEntry;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;

/**
 * Since the actual metadata can come in different formats depending on the number of values in it
 * it was decided that a more efficient approach would be to modify the object at deserialization.
 * Basically we are converting a map of &ltString,String&gt or List&ltString&gt which would have to
 * be replaced with &ltString,Object&gt to &ltString,List&ltString&gt&gt
 */
@Slf4j
public class MetadataDeserializer extends JsonDeserializer<Map<String, List<String>>> {

  @Override
  public Map<String, List<String>> deserialize(JsonParser jsonParser,
      DeserializationContext deserializationContext) throws IOException {
    log.debug("Deserializing metadata : jsonParser : {}, context : {}", jsonParser,
        deserializationContext);
    JsonNode node = jsonParser.getCodec().readTree(jsonParser);

    if (!node.fields().hasNext()) {
      return null;
    }

    Iterable<Entry<String, JsonNode>> iterable = node::fields;
    final Map<String, List<String>> metadataMap = StreamSupport
        .stream(iterable.spliterator(), false).map(MetadataEntry::new).collect(Collectors.toMap(
            MetadataEntry::getKey, MetadataEntry::getValue));

    log.debug("Result of deserialization of metadata : {} ", metadataMap);
    return metadataMap;
  }
}

