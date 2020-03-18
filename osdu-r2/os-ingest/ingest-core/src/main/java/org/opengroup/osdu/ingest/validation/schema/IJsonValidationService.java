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

package org.opengroup.osdu.ingest.validation.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ValidationMessage;
import java.util.Set;

public interface IJsonValidationService {

  /**
   * Validates given json against given schema.
   *
   * @param schema     schema that will be used to validate json
   * @param toValidate json string to validate against given schema
   * @return {@link Set} of validation messages
   */
  Set<ValidationMessage> validate(JsonNode schema, JsonNode toValidate);

}
