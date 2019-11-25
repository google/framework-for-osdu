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

package com.osdu.mapper;

import static com.osdu.request.OsduHeader.extractHeaderByName;
import static com.osdu.service.JsonUtils.toObject;

import com.fasterxml.jackson.core.type.TypeReference;
import com.osdu.model.IngestHeaders;
import com.osdu.request.OsduHeader;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.messaging.MessageHeaders;

public abstract class IngestHeadersMapperDecorator implements IngestHeadersMapper {

  @Override
  public IngestHeaders toIngestHeaders(MessageHeaders headers) {
    if (headers == null) {
      return null;
    }

    return IngestHeaders.builder()
        .authorizationToken(extractHeaderByName(headers, OsduHeader.AUTHORIZATION))
        .partition(extractHeaderByName(headers, OsduHeader.PARTITION))
        .legalTags(extractHeaderByName(headers, OsduHeader.LEGAL_TAGS))
        .resourceHomeRegionID(extractHeaderByName(headers, OsduHeader.RESOURCE_HOME_REGION_ID))
        .resourceHostRegionIDs(getResourceHostRegionIDs(
            extractHeaderByName(headers, OsduHeader.RESOURCE_HOST_REGION_IDS)))
        .build();
  }

  private static List<String> getResourceHostRegionIDs(String resourceHostRegionIDs) {
    return Optional.ofNullable(resourceHostRegionIDs)
        .map(regionIDs -> toObject(resourceHostRegionIDs, new TypeReference<List<String>>() {}))
        .orElse(Collections.emptyList());
  }

}
