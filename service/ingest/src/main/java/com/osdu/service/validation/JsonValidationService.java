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
