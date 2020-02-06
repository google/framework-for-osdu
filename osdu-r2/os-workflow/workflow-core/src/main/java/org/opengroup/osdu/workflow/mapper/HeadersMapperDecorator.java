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

package org.opengroup.osdu.workflow.mapper;

import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.lang3.RegExUtils;
import org.opengroup.osdu.core.common.model.Headers;
import org.springframework.messaging.MessageHeaders;

public abstract class HeadersMapperDecorator implements HeadersMapper {

  private static final Pattern PARTITION_PATTERN = Pattern.compile("[^a-zA-Z0-9]+");

  @Override
  public Headers toHeaders(MessageHeaders headers) {
    return Headers.builder()
        .authorizationToken(extractHeaderByName(headers, Headers.AUTHORIZATION))
        .partitionID(normalizePartitionID(extractHeaderByName(headers, Headers.PARTITION)))
        .build();
  }

  private static String extractHeaderByName(Map<String, Object> headers, String headerKey) {
    return (String) headers.get(headerKey);
  }

  private static String normalizePartitionID(String partition) {
    return RegExUtils.replaceAll(partition, PARTITION_PATTERN, "");
  }

}
