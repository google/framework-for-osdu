/*
 * Copyright 2020 Google LLC
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

package org.opengroup.osdu.delivery.util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import lombok.experimental.UtilityClass;
import org.opengroup.osdu.delivery.exception.OsduException;
import org.opengroup.osdu.delivery.model.jackson.ForceStringDeserializer;

@UtilityClass
public class JsonUtils {

  private static final ObjectMapper mapper;

  static {
    SimpleModule module = new SimpleModule();
    module.addDeserializer(String.class, new ForceStringDeserializer());
    mapper = new ObjectMapper()
        .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
        .disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
        .setSerializationInclusion(Include.NON_NULL)
        .registerModule(module)
        .findAndRegisterModules();
  }

  /**
   * Convert json string to object.
   * @param value json string
   * @param clazz object class
   * @return converted object
   */
  public <T> T toObject(String value, Class<T> clazz) {
    try {
      return mapper.readValue(value, clazz);
    } catch (IOException e) {
      throw new OsduException("Could not convert json string to object. String: " + value, e);
    }
  }

  /**
   * Convert json string to object using type reference.
   * @param value json string
   * @param typeReference type reference
   * @return converted object
   */
  public <T> T toObject(String value, TypeReference<T> typeReference) {
    try {
      return mapper.readValue(value, typeReference);
    } catch (IOException e) {
      throw new OsduException("Could not convert json string to object. String: " + value, e);
    }
  }

  /**
   * Returns String in JSON format from input object.
   * @param value any type
   * @return String of json object
   */
  public <T> String toJson(T value) {
    try {
      return mapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new OsduException("Could not convert object to JSON. Object: " + value, e);
    }
  }

  /**
   * Deep copy object with type T to object with type R using JSON.
   * @param object object for copy
   * @param clazz object class
   * @param <T> source object type
   * @param <R> dest object type
   * @return deep copy object with type R
   */
  public <T, R> R deepCopy(T object, Class<R> clazz) {
    return toObject(toJson(object), clazz);
  }

}
