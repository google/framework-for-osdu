package com.osdu.model.delfi.geo;

import com.osdu.model.delfi.geo.exception.GeoLocationException;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ByGeoPolygon implements GeoLocation {

    private Point[] points;

    public ByGeoPolygon(Object[] coordinates) throws GeoLocationException {
        if (coordinates.length < 3) {
            throw new GeoLocationException("Polygon GeoJSON requires at least 3 points for creation, actual, received " + coordinates.length);
        }
        List<Point> points = new ArrayList<>();
        for (Object coordinate : coordinates) {
            points.add(GeoUtils.coordinatesToPoint(coordinate));
        }
        points.toArray(this.points);
    }

}
