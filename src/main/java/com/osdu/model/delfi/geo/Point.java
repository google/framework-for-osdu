package com.osdu.model.delfi.geo;

import lombok.Data;
import lombok.NonNull;

@Data
public class Point {

    @NonNull
    private Double latitude;
    @NonNull
    private Double longitude;

}
