package com.osdu.request;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class OsduHeader {

  private OsduHeader() {
  }

  public static final String PARTITION = "partition-id";
  public static final String AUTHORIZATION = "authorization";
  public static final String LEGAL_TAGS = "legal-tags";
  public static final String RESOURCE_HOME_REGION_ID = "ResourceHomeRegionID";
  public static final String RESOURCE_HOST_REGION_IDS = "ResourceHostRegionIDs";

  /**
   * Extract header by name.
   *
   * @param headers   headers from http request
   * @param headerKey header key
   * @return header value
   */
  public static String extractHeaderByName(Map<String, Object> headers, String headerKey) {
    log.debug("Extracting header with name : {} from map : {}", headerKey, headers);
    String value = (String) headers.get(headerKey);
    log.debug("Does the request contain the '{}' header? {}. Value: {}",
        headerKey, headers.containsKey(headerKey), value);
    return value;
  }

}
