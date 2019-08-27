package com.osdu.model.delfi;

import com.osdu.model.SearchObject;
import com.osdu.model.delfi.geo.SpatialFilter;
import lombok.Data;

import java.util.List;

@Data
public class DelfiSearchObject extends SearchObject {

    private String kind;
    private Integer limit;
    private String query;
    private List<String> returnedFields;
    private Sort sort;
    private Boolean queryAsOwner;
    private SpatialFilter spatialFilter;
    private Integer offset;

}
