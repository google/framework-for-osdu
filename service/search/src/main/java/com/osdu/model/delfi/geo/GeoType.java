package com.osdu.model.delfi.geo;

import com.osdu.model.delfi.geo.exception.GeoLocationException;
import lombok.Getter;

@Getter
public enum GeoType {
  BY_BOUNDING_BOX("byBoundingBox"), BY_DISTANCE("byDistance"),
  BY_GEO_POLYGON("byGeoPolygon"), POINT("point"), POLYGON("polygon");

  final String typeFieldName;

  GeoType(String typeFieldName) {
    this.typeFieldName = typeFieldName;
  }

  public static GeoType lookup(String fieldName) throws GeoLocationException {
    for (GeoType value : GeoType.values()) {
      if (value.getTypeFieldName().equals(fieldName)) {
        return value;
      }
    }
    throw new GeoLocationException("GeoLocation data is present, but type is unknown.");
  }
}
