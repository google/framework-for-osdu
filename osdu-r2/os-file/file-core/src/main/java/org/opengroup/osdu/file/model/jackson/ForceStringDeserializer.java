/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.file.model.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import org.springframework.boot.jackson.JsonComponent;

@JsonComponent
public class ForceStringDeserializer extends JsonDeserializer<String> {

  @Override
  public String deserialize(JsonParser parser,
      DeserializationContext ctxt) throws IOException, JsonProcessingException {
    if (parser.getCurrentToken() == JsonToken.VALUE_NUMBER_INT) {
      throw ctxt.wrongTokenException(parser, this.handledType(), JsonToken.VALUE_STRING,
          "Attempted to parse int to string but this is forbidden");
    }
    return parser.getValueAsString();
  }
}
