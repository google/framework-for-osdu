/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.osdu.model.deserializer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.collect.Lists;
import com.osdu.exception.SearchException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class MetadataEntry {

  String key;
  List<String> value;

  /**
   * Creates a metadata entry hiding all underlying actions needed in order to "flatten" metadata to
   * a single-type collection.
   *
   * @param metadataEntry metadata entry
   */
  public MetadataEntry(Map.Entry<String, JsonNode> metadataEntry) {
    this.key = metadataEntry.getKey();

    final ArrayList<JsonNode> elementsOfTheNode = Lists
        .newArrayList(metadataEntry.getValue().elements());
    if (isArrayNode(metadataEntry.getValue(), elementsOfTheNode)) {
      try {
        ObjectReader reader = new ObjectMapper().reader()
            .forType(new TypeReference<List<String>>() {
            });
        this.value = reader.readValue(metadataEntry.getValue());
      } catch (IOException e) {
        throw new SearchException(String
            .format("Failed to create MetadataEntry with following key : %s and value :%s", key,
                value));
      }
    } else if (isTextNode(metadataEntry.getValue(), elementsOfTheNode)) {
      this.value = Collections.singletonList(metadataEntry.getValue().asText());
    } else {
      throw new SearchException(String
          .format("Failed to create MetadataEntry with following key : %s and value :%s", key,
              value));
    }
  }

  /**
   * Checks is the provided Entry is actually a text.
   *
   * @param jsonNode          jsonNode to be evaluated
   * @param elementsOfTheNode list of it's elements in a convenient form
   * @return true - if the element is a text entry, false if it isn't
   */
  protected boolean isTextNode(JsonNode jsonNode,
      ArrayList<JsonNode> elementsOfTheNode) {
    return elementsOfTheNode.isEmpty()
        && jsonNode instanceof TextNode;
  }

  /**
   * Checks if the provided entity is actually an array of text values.
   *
   * @param jsonNode          jsonNode to be evaluated
   * @param elementsOfTheNode list of it's elements in a convenient form
   * @return true - if the element is a text entry, false if it isn't
   */
  protected boolean isArrayNode(JsonNode jsonNode, ArrayList<JsonNode> elementsOfTheNode) {
    return !elementsOfTheNode.isEmpty() && elementsOfTheNode.stream()
        .allMatch(node -> node instanceof TextNode)
        && jsonNode instanceof ArrayNode;
  }
}