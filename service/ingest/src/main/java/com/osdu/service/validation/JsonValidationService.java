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
