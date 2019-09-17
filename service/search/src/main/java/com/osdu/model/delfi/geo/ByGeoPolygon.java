package com.osdu.model.delfi.geo;

import com.osdu.model.delfi.geo.exception.GeoLocationException;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class ByGeoPolygon implements GeoLocation {

  Point[] points;

  /**
   * Constructor.
   * @param coordinates coordinates
   */
  public ByGeoPolygon(Object[] coordinates) {
    if (coordinates.length < 3) {
      throw new GeoLocationException(
          "Polygon GeoJSON requires at least 3 points for creation, actual, received "
              + coordinates.length);
    }
    List<Point> pointsList = new ArrayList<>();
    for (Object coordinate : coordinates) {
      pointsList.add(GeoUtils.coordinatesToPoint(coordinate));
    }
    pointsList.toArray(this.points);
  }

}
