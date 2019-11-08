/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.osdu.mapper;

import static com.osdu.model.delfi.geo.GeoType.BY_BOUNDING_BOX;
import static com.osdu.model.delfi.geo.GeoType.BY_GEO_POLYGON;
import static org.assertj.core.api.Assertions.assertThat;

import com.osdu.model.delfi.DelfiSearchObject;
import com.osdu.model.delfi.geo.ByBoundingBox;
import com.osdu.model.delfi.geo.ByDistance;
import com.osdu.model.delfi.geo.ByGeoPolygon;
import com.osdu.model.osdu.GeoLocation;
import com.osdu.model.osdu.OsduSearchObject;
import com.osdu.model.osdu.SortOption;
import com.osdu.model.osdu.SortOption.OrderType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Named;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SearchObjectMapperDecoratorTest {

  private static final String KIND = "kind";
  private static final String PARTITION = "partition";
  private static final String ASC = "asc";
  private static final String DESC = "desc";
  private static final String DATA_FIELD_1 = "data.field1";
  private static final String DATA_FIELD_2 = "data.field2";

  @Autowired
  @Named
  SearchObjectMapper searchObjectMapper;

  @Test
  public void shouldMapGeoCentroid() {

    // given
    OsduSearchObject osduSearchObject = new OsduSearchObject();
    osduSearchObject.setStart(1);
    osduSearchObject.setCount(2);

    osduSearchObject.setGeoCentroid(new List[]{Arrays.asList(36.742612, -99.074218)});

    // when
    DelfiSearchObject delfiSearchObject = searchObjectMapper.osduToDelfi(osduSearchObject, KIND,
        PARTITION);

    // then
    assertThat(delfiSearchObject).isNotNull();
    assertThat(delfiSearchObject.getKind()).isEqualTo(KIND);
    assertThat(delfiSearchObject.getLimit()).isEqualTo(2);
    assertThat(delfiSearchObject.getOffset()).isEqualTo(1);
    assertThat(delfiSearchObject.getSpatialFilter().getField()).isEqualTo("data.dlLatLongWGS84");
    ByDistance location = (ByDistance) delfiSearchObject.getSpatialFilter().getByDistance();
    assertThat(location.getDistance()).isEqualTo(1000);
    assertThat(location.getPoint().getLatitude()).isEqualTo(36.742612);
    assertThat(location.getPoint().getLongitude()).isEqualTo(-99.074218);
  }

  @Test
  public void shouldMapGeoLocationByBoundingBox() {

    // given
    GeoLocation osduLocation = new GeoLocation();
    osduLocation.setDistance(1000D);
    osduLocation.setCoordinates(new List[]{Arrays.asList(36.742612, -99.074218), Arrays.asList(42.54, 36.18)});
    osduLocation.setType(BY_BOUNDING_BOX.getTypeFieldName());

    OsduSearchObject osduSearchObject = new OsduSearchObject();
    osduSearchObject.setGeoLocation(osduLocation);

    // when
    DelfiSearchObject delfiSearchObject = searchObjectMapper.osduToDelfi(osduSearchObject, KIND,
        PARTITION);

    // then
    assertThat(delfiSearchObject).isNotNull();
    assertThat(delfiSearchObject.getSpatialFilter().getField()).isEqualTo("data.dlLatLongWGS84");
    ByBoundingBox location = (ByBoundingBox) delfiSearchObject.getSpatialFilter().getByBoundingBox();
    assertThat(location.getTopLeft().getLatitude()).isEqualTo(36.742612);
    assertThat(location.getTopLeft().getLongitude()).isEqualTo(-99.074218);
    assertThat(location.getBottomRight().getLatitude()).isEqualTo(42.54);
    assertThat(location.getBottomRight().getLongitude()).isEqualTo(36.18);
  }

  @Test
  public void shouldMapGeoLocationByGeoPolygon() {

    // given
    GeoLocation osduLocation = new GeoLocation();
    osduLocation.setDistance(1000D);
    osduLocation.setCoordinates(new List[]{Arrays.asList(36.742612, -99.074218),
        Arrays.asList(47.54, 37.18),
        Arrays.asList(48.54, 38.18),
        Arrays.asList(49.54, 39.18)});
    osduLocation.setType(BY_GEO_POLYGON.getTypeFieldName());

    OsduSearchObject osduSearchObject = new OsduSearchObject();
    osduSearchObject.setGeoLocation(osduLocation);

    // when
    DelfiSearchObject delfiSearchObject = searchObjectMapper.osduToDelfi(osduSearchObject, KIND,
        PARTITION);

    // then
    assertThat(delfiSearchObject).isNotNull();
    assertThat(delfiSearchObject.getSpatialFilter().getField()).isEqualTo("data.dlLatLongWGS84");
    ByGeoPolygon location = (ByGeoPolygon) delfiSearchObject.getSpatialFilter().getByGeoPolygon();
    assertThat(location.getPoints().get(0).getLatitude()).isEqualTo(36.742612);
    assertThat(location.getPoints().get(0).getLongitude()).isEqualTo(-99.074218);
    assertThat(location.getPoints().get(1).getLatitude()).isEqualTo(47.54);
    assertThat(location.getPoints().get(1).getLongitude()).isEqualTo(37.18);
    assertThat(location.getPoints().get(2).getLatitude()).isEqualTo(48.54);
    assertThat(location.getPoints().get(2).getLongitude()).isEqualTo(38.18);
    assertThat(location.getPoints().get(3).getLatitude()).isEqualTo(49.54);
    assertThat(location.getPoints().get(3).getLongitude()).isEqualTo(39.18);
  }

  @Test
  public void shouldMapSort() {

    // given
    SortOption sortOption1 = new SortOption();
    sortOption1.setFieldName(DATA_FIELD_1);
    sortOption1.setOrderType(OrderType.ASC);

    SortOption sortOption2 = new SortOption();
    sortOption2.setFieldName(DATA_FIELD_2);
    sortOption2.setOrderType(OrderType.DESC);

    OsduSearchObject osduSearchObject = new OsduSearchObject();
    osduSearchObject.setSort(new SortOption[]{sortOption1, sortOption2});

    // when
    DelfiSearchObject delfiSearchObject = searchObjectMapper.osduToDelfi(osduSearchObject, KIND,
        PARTITION);

    // then
    assertThat(delfiSearchObject).isNotNull();
    assertThat(delfiSearchObject.getSort().getField()).containsExactly(DATA_FIELD_1, DATA_FIELD_2);
    assertThat(delfiSearchObject.getSort().getOrder()).containsExactly(ASC, DESC);
  }

  @Test
  public void shouldMapQuery() {

    // given
    OsduSearchObject osduSearchObject = new OsduSearchObject();
    Map<String, List<String>> query = new HashMap<>();
    query.put("data", Arrays.asList("value"));
    query.put("data1", Arrays.asList("value11", "value12"));
    osduSearchObject.setMetadata(query);
    osduSearchObject.setFulltext("full-text-query");

    // when
    DelfiSearchObject delfiSearchObject = searchObjectMapper.osduToDelfi(osduSearchObject, KIND,
        PARTITION);

    // then
    assertThat(delfiSearchObject).isNotNull();
    assertThat(delfiSearchObject.getQuery())
        .isEqualTo("full-text-query AND (data : \"value\") AND (data1 : \"value11\" OR data1 : \"value12\")");
  }
}
