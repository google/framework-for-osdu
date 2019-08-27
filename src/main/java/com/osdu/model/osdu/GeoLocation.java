package com.osdu.model.osdu;

import lombok.Data;

/**
 * Wrapper class for OSDU GeoLocation object.
 * coordinates are an object array due to different possible variations of incoming object - for point it can be just
 * an array of two doubles, but for polygon it will be an array of arrays of doubles located in the same property of the JSON file.
 */
@Data
public class GeoLocation {

    private Double distance;
    private String type;
    private Object[] coordinates;

}
