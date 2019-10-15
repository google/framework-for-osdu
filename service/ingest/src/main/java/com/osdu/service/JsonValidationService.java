package com.osdu.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.core.report.ProcessingReport;

public interface JsonValidationService {

  ProcessingReport validate(JsonNode schema, JsonNode toValidate);
}
