package com.osdu.model.delfi.geo;

import lombok.Data;
import lombok.NonNull;

@Data
public class Point {

  @NonNull
  Double latitude;
  @NonNull
  Double longitude;

}
