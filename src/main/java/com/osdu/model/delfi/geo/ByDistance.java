package com.osdu.model.delfi.geo;

public class ByDistance implements GeoLocation {

    public static final String CURRENT_TYPE = "byDistance";

    @Override
    public String getCurrentType(String type) {
        return CURRENT_TYPE;
    }
}
