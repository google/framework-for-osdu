package com.osdu.model.delfi.geo;

import com.osdu.model.delfi.geo.exception.GeoLocationException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;

@Data
public class ByGeoPolygon implements GeoLocation {

  List<Point> points;

  /**
   * Constructor.
   *
   * @param coordinates coordinates
   */
  public ByGeoPolygon(Object[] coordinates) {
    if (coordinates.length < 3) {
      throw new GeoLocationException(String.format(
          "Polygon GeoJSON requires at least 3 points for creation, actual, received : %s ",
          coordinates.length));
    }
    points = Arrays.stream(coordinates)
        .map(GeoUtils::coordinatesToPoint).collect(Collectors.toList());
  }
}
