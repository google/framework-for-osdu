package com.osdu.model.delfi.geo;

import java.util.List;

public class GeoUtils {

  public static Point coordinatesToPoint(Object coordinates) {
    List<Double> pointCoordinates = (List<Double>) coordinates;
    return new Point(pointCoordinates.get(0), pointCoordinates.get(1));
  }
}
