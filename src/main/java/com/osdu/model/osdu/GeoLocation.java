package com.osdu.model.osdu;

import com.google.common.base.Objects;

import java.util.Arrays;

public class GeoLocation {
    public static final String BY_BOUNDING_BOX_GEO_TYPE = "point";

    private String type;
    private Double[] cooridanates;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double[] getCooridanates() {
        return cooridanates;
    }

    public void setCooridanates(Double[] cooridanates) {
        this.cooridanates = cooridanates;
    }

    @Override
    public String toString() {
        return "GeoLocation{" +
                "type='" + type + '\'' +
                ", cooridanates=" + Arrays.toString(cooridanates) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeoLocation that = (GeoLocation) o;
        return Objects.equal(type, that.type) &&
                Objects.equal(cooridanates, that.cooridanates);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(type, cooridanates);
    }
}
