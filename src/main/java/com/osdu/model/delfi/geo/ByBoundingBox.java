package com.osdu.model.delfi.geo;

import com.osdu.model.delfi.Point;

public class ByBoundingBox implements GeoLocation {

    public static final String CURRENT_TYPE = "byBoundingBox";

    private Point topLeft;
   private Point bottomRight;

    public ByBoundingBox(Point topLeft, Point bottomRight) {
        this.topLeft = topLeft;
        this.bottomRight = bottomRight;
    }

    public Point getTopLeft() {
        return topLeft;
    }

    public void setTopLeft(Point topLeft) {
        this.topLeft = topLeft;
    }

    public Point getBottomRight() {
        return bottomRight;
    }

    public void setBottomRight(Point bottomRight) {
        this.bottomRight = bottomRight;
    }

    @Override
    public String getCurrentType(String type) {
        return CURRENT_TYPE;
    }
}
