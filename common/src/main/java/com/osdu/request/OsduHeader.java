package com.osdu.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessageHeaders;

@Slf4j
public final class OsduHeader {

  private OsduHeader() {
  }

  public static final String PARTITION = "partition";
  public static final String AUTHORIZATION = "authorization";
  public static final String LEGAL_TAGS = "legaltags";
  public static final String RESOURCE_HOME_REGION_ID = "ResourceHomeRegionID";
  public static final String RESOURCE_HOST_REGION_IDS = "ResourceHostRegionIDs";

  public static String extractHeaderByName(MessageHeaders headers, String headerKey) {
    log.debug("Extracting header with name : {} from map : {}", headerKey, headers);
    if (headers.containsKey(headerKey)) {
      String result = (String) headers.get(headerKey);
      log.debug("Found header in the request with following key:value pair : {}:{}", headerKey,
          result);
      return result;
    }
    log.debug("Not found header in the request with following key:{}", headerKey);
    return null;
  }

}
