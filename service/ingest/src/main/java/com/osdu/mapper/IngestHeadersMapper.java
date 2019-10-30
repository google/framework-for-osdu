package com.osdu.mapper;

import static com.osdu.request.OsduHeader.extractHeaderByName;

import com.osdu.model.IngestHeaders;
import com.osdu.request.OsduHeader;
import org.mapstruct.Mapper;
import org.springframework.messaging.MessageHeaders;

@Mapper
public interface IngestHeadersMapper {

  /**
   * Transforms message headers from the received request to Delfi format
   * @param headers MessageHeaders object from the request
   * @return headers that will be accepted by delfi portal
   */
  default IngestHeaders toIngestHeaders(MessageHeaders headers) {
    if (headers == null) {
      return null;
    }

    return IngestHeaders.builder()
        .authorizationToken(extractHeaderByName(headers, OsduHeader.AUTHORIZATION))
        .partition(extractHeaderByName(headers, OsduHeader.PARTITION))
        .legalTags(extractHeaderByName(headers, OsduHeader.LEGAL_TAGS))
        .homeRegionID(extractHeaderByName(headers, OsduHeader.RESOURCE_HOME_REGION_ID))
        .hostRegionIDs(extractHeaderByName(headers, OsduHeader.RESOURCE_HOST_REGION_IDS))
        .build();
  }

}
