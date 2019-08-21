package com.osdu.model.delfi.geo;

public class ByGeoPolygon implements GeoLocation {

    public static final String CURRENT_TYPE = "byGeoPolygon";

    @Override
    public String getCurrentType(String type) {
        return CURRENT_TYPE;
    }
}
