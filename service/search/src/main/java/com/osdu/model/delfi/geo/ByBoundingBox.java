package com.osdu.model.delfi.geo;

import com.osdu.model.delfi.geo.exception.GeoLocationException;
import lombok.Data;
import lombok.NonNull;

@Data
public class ByBoundingBox implements GeoLocation {

  @NonNull
  Point topLeft;
  @NonNull
  Point bottomRight;

  public ByBoundingBox(Object[] coordinates) throws GeoLocationException {
    if (coordinates.length != 2) {
      throw new GeoLocationException(
          "Bounding box GeoJSON requires exactly 2 points for creation, actual, received "
              + coordinates.length);
    }
    topLeft = GeoUtils.coordinatesToPoint(coordinates[0]);
    bottomRight = GeoUtils.coordinatesToPoint(coordinates[1]);
  }
}
