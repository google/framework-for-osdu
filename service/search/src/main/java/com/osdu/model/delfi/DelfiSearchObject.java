package com.osdu.model.delfi;

import com.osdu.model.SearchObject;
import com.osdu.model.delfi.geo.SpatialFilter;
import java.util.List;
import lombok.Data;

@Data
public class DelfiSearchObject extends SearchObject {

  String kind;
  Integer limit;
  String query;
  List<String> returnedFields;
  Sort sort;
  Boolean queryAsOwner;
  SpatialFilter spatialFilter;
  Integer offset;

}
