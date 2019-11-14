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

package com.osdu.service.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonMetaSchema;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaException;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import com.osdu.exception.IngestException;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class JsonValidationService {

  /**
   * Validates given json against given schema.
   *
   * @param schema     schema that will be used to validate json
   * @param toValidate json string to validate against given schema
   * @return {@link Set} of validation messages
   */
  public Set<ValidationMessage> validate(String schema, JsonNode toValidate) {
    try {
      JsonSchema jsonSchema = JsonSchemaFactory.getInstance().getSchema(schema);

      return jsonSchema.validate(toValidate);
    } catch (JsonSchemaException e) {
      throw new IngestException(
          String.format("Error creating json validation schema from json object: %s", schema), e);
    }
  }

  /**
   * Validates given json against given schema.
   *
   * @param schema     schema that will be used to validate json
   * @param toValidate json string to validate against given schema
   * @return {@link Set} of validation messages
   */
  public Set<ValidationMessage> validate(JsonNode schema, JsonNode toValidate) {
    try {
      JsonSchemaFactory factory = JsonSchemaFactory.builder(JsonSchemaFactory.getInstance())
          .addMetaSchema(JsonMetaSchema
              .builder("http://json-schema.org/draft-07/schema#", JsonMetaSchema.getDraftV4())
              .build())
          .build();
      JsonSchema jsonSchema = factory.getSchema(schema);

      return jsonSchema.validate(toValidate);
    } catch (JsonSchemaException e) {
      throw new IngestException(
          String.format("Error creating json validation schema from json object: %s", schema), e);
    }
  }

}
