package com.osdu.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.osdu.exception.OsduException;
import java.io.IOException;

public final class JsonUtils {

  static ObjectMapper mapper = new ObjectMapper();

  /**
   *  Returns JsonNode from input object.
   * @param value any type
   * @return JsonNode from input object
   */
  public static <T> JsonNode getJsonNode(T value) {
    try {
      return mapper.readTree(toJson(value));
    } catch (IOException e) {
      throw new OsduException(
          String.format("Could not convert object to JSON. Object : %s", value), e);
    }
  }

  /**
   * Returns String in JSON format from input object.
   * @param value any type
   * @return String of json object
   */
  public static <T> String toJson(T value) {
    try {
      return mapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new OsduException("Could not convert object to JSON. Object: " + value);
    }
  }

}
