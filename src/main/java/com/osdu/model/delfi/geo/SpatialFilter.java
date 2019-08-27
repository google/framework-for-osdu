package com.osdu.model.delfi.geo;

import lombok.Data;

/**
 * GeoLocation object descriptor used by Delfi API.
 */
@Data
public class SpatialFilter {

    private static final String GEO_LOCATION_FIELD_ID = "data.dlLatLongWGS84";

    private String field = GEO_LOCATION_FIELD_ID;
    //Delfi spec assumes that there can be 1 of 3 different types of objects and in order to maintain that we need this property
    private GeoLocation byBoundingBox;
    private GeoLocation byDistance;
    private GeoLocation byGeoPolygon;

}
