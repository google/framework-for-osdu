package com.osdu.model.delfi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import com.osdu.model.delfi.geo.GeoLocation;

/**
 * GeoLocation object descriptor used by Delfi API.
 */
public class SpatialFilter {

    private static final String GEO_LOCATION_FIELD_ID = "data.dlLatLongWGS84";

    private String field = GEO_LOCATION_FIELD_ID;
    @JsonIgnore
    private String type;
    //Delfi spec assumes that there can be 1 of 3 different types of objects and in order to maintain that we need this property
    //TODO: Replace with custom writer later ( interface -> dynamic json-alias to set name based on the actual object used )
    private GeoLocation byBoundingBox;
    private GeoLocation byDistance;
    private GeoLocation byGeoPolygon;


    public GeoLocation getByBoundingBox() {
        return byBoundingBox;
    }

    public void setByBoundingBox(GeoLocation byBoundingBox) {
        this.byBoundingBox = byBoundingBox;
    }

    public GeoLocation getByDistance() {
        return byDistance;
    }

    public void setByDistance(GeoLocation byDistance) {
        this.byDistance = byDistance;
    }

    public GeoLocation getByGeoPolygon() {
        return byGeoPolygon;
    }

    public void setByGeoPolygon(GeoLocation byGeoPolygon) {
        this.byGeoPolygon = byGeoPolygon;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpatialFilter that = (SpatialFilter) o;
        return Objects.equal(field, that.field) &&
                Objects.equal(type, that.type) &&
                Objects.equal(byBoundingBox, that.byBoundingBox) &&
                Objects.equal(byDistance, that.byDistance) &&
                Objects.equal(byGeoPolygon, that.byGeoPolygon);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(field, type, byBoundingBox, byDistance, byGeoPolygon);
    }

    @Override
    public String toString() {
        return "SpatialFilter{" +
                "field='" + field + '\'' +
                ", type='" + type + '\'' +
                ", byBoundingBox=" + byBoundingBox +
                ", byDistance=" + byDistance +
                ", byGeoPolygon=" + byGeoPolygon +
                '}';
    }
}
