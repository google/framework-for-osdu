package com.osdu.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IngestHeaders {

  String authorizationToken;
  String partition;
  String legalTags;
  String homeRegionID;
  String hostRegionIDs;

}
