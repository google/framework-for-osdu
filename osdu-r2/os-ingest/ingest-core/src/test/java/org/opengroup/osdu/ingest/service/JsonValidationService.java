/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.ingest.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonMetaSchema;
import com.networknt.schema.JsonSchemaException;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import java.util.Set;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JsonValidationService {

  public Set<ValidationMessage> validate(JsonNode schema, JsonNode toValidate) {
    try {
      return getFactory()
          .getSchema(schema)
          .validate(toValidate);
    } catch (JsonSchemaException e) {
      throw new RuntimeException(
          String.format("Error creating json validation schema from json object: %s", schema), e);
    }
  }

  private JsonSchemaFactory getFactory() {
    return JsonSchemaFactory.builder(JsonSchemaFactory.getInstance())
        .addMetaSchema(JsonMetaSchema
            .builder("http://json-schema.org/draft-07/schema#", JsonMetaSchema.getDraftV4())
            .build())
        .build();
  }
}
