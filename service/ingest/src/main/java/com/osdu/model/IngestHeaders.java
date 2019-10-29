package com.osdu.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngestHeaders {

  String authorizationToken;
  String partition;
  String legalTags;
  String homeRegionID;
  String hostRegionIDs;

}
