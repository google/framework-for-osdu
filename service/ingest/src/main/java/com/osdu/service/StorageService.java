package com.osdu.service;

import com.fasterxml.jackson.databind.JsonNode;

public interface StorageService {

  JsonNode getSchemaByLink(String schemaLink);
}
