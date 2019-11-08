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
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.osdu.exception.IngestException;
import org.springframework.stereotype.Service;


@Service
public class JsonValidationService {

  /**
   * Validates given json against given schema.
   * @param schemaJson schema that will be used to validate json
   * @param toValidate json node to validate against given schema
   * @return report with the result and a list of errors and warnings (if any)
   */
  public ProcessingReport validate(JsonNode schemaJson, JsonNode toValidate) {
    try {
      JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
      JsonSchema schema = factory.getJsonSchema(schemaJson);

      return schema.validate(toValidate);

    } catch (ProcessingException e) {
      throw new IngestException(
          String.format("Error creating json validation schema from json object: %s",
              schemaJson.asText()), e);
    }
  }
}
