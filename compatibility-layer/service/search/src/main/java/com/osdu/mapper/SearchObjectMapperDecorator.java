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

import static java.lang.String.format;

import com.osdu.model.delfi.DelfiSearchObject;
import com.osdu.model.delfi.Sort;
import com.osdu.model.delfi.geo.ByBoundingBox;
import com.osdu.model.delfi.geo.ByDistance;
import com.osdu.model.delfi.geo.ByGeoPolygon;
import com.osdu.model.delfi.geo.GeoType;
import com.osdu.model.delfi.geo.SpatialFilter;
import com.osdu.model.delfi.geo.exception.GeoLocationException;
import com.osdu.model.osdu.GeoLocation;
import com.osdu.model.osdu.OsduSearchObject;
import com.osdu.model.osdu.SortOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Slf4j
public abstract class SearchObjectMapperDecorator implements SearchObjectMapper {

  // Business logic - what default distance? Zero causes error on Delfi
  private static final double DEFAULT_DISTANCE = 1000.0;
  private static final String LUCENE_AND_TERM = " AND ";
  private static final String LUCENE_OR_TERM = " OR ";
  private static final String BY_BOUNDING_BOX_GEOLOCATION_TYPE = "ByBoundingBox";

  @Inject
  @Named("com.osdu.mapper.SearchObjectMapperImpl_")
  SearchObjectMapper delegate;

  @Override
  public DelfiSearchObject osduToDelfi(OsduSearchObject osduSearchObject,
      String kind, String partition) {
    log.debug("Mapping request for object : {}", osduSearchObject);
    DelfiSearchObject result = delegate
        .osduToDelfi(osduSearchObject, kind, partition);
    addToQuery(result, osduSearchObject.getFulltext(), mapMetadata(osduSearchObject));
    result.setKind(kind);

    if (osduSearchObject.getGeoLocation() != null) {
      result.setSpatialFilter(mapGeoParameters(osduSearchObject.getGeoLocation()));
    } else if (osduSearchObject.getGeoCentroid() != null) {
      result.setSpatialFilter(mapGeoParameters(osduSearchObject.getGeoCentroid()));
    }
    result.setSort(mapSort(osduSearchObject.getSort()));
    log.debug("Result of mapping : {}", result);
    return result;
  }

  private Sort mapSort(List<SortOption> sortOptions) {
    log.debug("Mapping sort object: {}", sortOptions);
    if (sortOptions != null) {
      Sort sort = osduToDelfiSort(sortOptions);
      log.debug("Result of mapping: {}", sort);
      return sort;
    }
    return null;
  }

  private SpatialFilter mapGeoParameters(List<List<Double>> geoCentroidList) {
    log.debug("Mapping geoCentroid object: {}", geoCentroidList);
    SpatialFilter spatialFilter = mapGeoCentroidObject(geoCentroidList);
    log.debug("Result of mapping: {}", spatialFilter);
    return spatialFilter;
  }

  private SpatialFilter mapGeoParameters(GeoLocation geoLocation) {
    log.debug("Mapping geoLocation object: {}", geoLocation);
    SpatialFilter spatialFilter = mapGeoLocationObject(geoLocation);
    log.debug("Result of mapping: {}", spatialFilter);
    return spatialFilter;
  }

  private String mapMetadata(OsduSearchObject osduSearchObject) {
    log.debug("Mapping metadata object: {}", osduSearchObject.getMetadata());
    if (osduSearchObject.getMetadata() != null) {

      final String result = osduSearchObject.getMetadata().entrySet().stream()
          .map(queryEntry -> queryEntry.getValue().stream()
              .map(queryKeyValuePair ->
                  format("(%s : \"%s\")" + LUCENE_OR_TERM + "(%s : \"%s\")",
                      getModifiedKey(queryEntry), queryKeyValuePair,
                      queryEntry.getKey(), queryKeyValuePair))
              .collect(Collectors.joining(LUCENE_OR_TERM))).map(queryTerm -> "(" + queryTerm + ")")
          .collect(Collectors.joining(LUCENE_AND_TERM));
      log.debug("Result of mapping: {}", result);
      return result;
    }
    return null;
  }

  private String getModifiedKey(Entry<String, List<String>> queryEntry) {
    return queryEntry.getKey().replaceAll("(^data[.])", "data.osdu.");
  }

  /**
   * Maps sort objects. This is not done via mapstruct since the objects are very different ( they
   * have different structure ) and at the same time they do not have common fields that could at
   * least partially justify the reason for creating a mapper for them.
   *
   * @param sortOptions sort option
   * @return Sort object
   */
  private Sort osduToDelfiSort(List<SortOption> sortOptions) {
    List<String> fields = new ArrayList<>();
    List<String> orders = new ArrayList<>();

    for (int i = 0; i < sortOptions.size(); i++) {
      fields.add(sortOptions.get(i).getFieldName());
      orders.add(sortOptions.get(i).getOrderType().toString().toLowerCase());
    }

    Sort sort = new Sort();
    sort.setField(fields);
    sort.setOrder(orders);
    return sort;
  }

  /**
   * Map GeoCentroid. Not mapped via mapstruct for same reasons as geoLocation, but in this case
   * mapping of this field is optional and based on the mapping of the geoLocation object
   *
   * @param geoCentroidList points for geo centroid
   * @return Delfi GeoLocation
   */
  private SpatialFilter mapGeoCentroidObject(List<List<Double>> geoCentroidList) {
    SpatialFilter spatialFilter = new SpatialFilter();

    //there is no direct match between OSDU GeoCentroid and Delfi GeoLocation.
    //but as a fallback measure we decided to infer the value of data from this field in case
    //we don't have data in the GeoLocation OSDU field.
    //1 - One Point is present - this can only be a Point type
    //2 - This is a unique "BoundingBox" type that is not present in RFC for GeoJson
    //3+- This is something else. But given that we know other types that can be used by
    // Delfi Portal this is the only possible option.
    switch (geoCentroidList.size()) {
      case 1:
        spatialFilter.setByDistance(
            new ByDistance(geoCentroidList, DEFAULT_DISTANCE));
        break;
      case 2:
        spatialFilter.setByBoundingBox(new ByBoundingBox(geoCentroidList));
        break;
      default:
        spatialFilter.setByGeoPolygon(new ByGeoPolygon(geoCentroidList));
    }
    return spatialFilter;
  }

  /**
   * Manually map sort objects. They are not extracted into mapstruct mappers since they are quite
   * small but have completely different format.
   *
   * @param geoLocation - OSDU GeoLocation object to extract GeoData from
   * @return extracted geo data
   */
  private SpatialFilter mapGeoLocationObject(GeoLocation geoLocation) {
    if (geoLocation.getCoordinates() == null) {
      log.warn(
          "No coordinates were given to geo location search request. GeoLocation - " + geoLocation);
      throw new GeoLocationException("Invalid parameters were given on search request");
    }

    SpatialFilter spatialFilter = new SpatialFilter();
    switch (GeoType.lookup(geoLocation.getType())) {
      case BY_BOUNDING_BOX:
        spatialFilter.setByBoundingBox(
            new ByBoundingBox(geoLocation.getCoordinates()));
        break;
      case BY_DISTANCE:
        spatialFilter.setByDistance(
            new ByDistance(geoLocation.getCoordinates(),
                geoLocation.getDistance()));
        break;
      case POLYGON:
      case BY_GEO_POLYGON:
        spatialFilter
            .setByGeoPolygon(new ByGeoPolygon(geoLocation.getCoordinates()));
        break;
      case POINT:
        spatialFilter.setByDistance(
            new ByDistance(geoLocation.getCoordinates(),
                DEFAULT_DISTANCE));
        break;
      default:
        log.warn("Not defined geo type for " + GeoType.lookup(geoLocation.getType()));
    }
    if (geoLocation.getType().equals(BY_BOUNDING_BOX_GEOLOCATION_TYPE)) {
      spatialFilter
          .setByBoundingBox(new ByBoundingBox(geoLocation.getCoordinates()));
    }
    return spatialFilter;
  }

  private void addToQuery(DelfiSearchObject delfiSearchObject, String... searchTerms) {
    for (String searchTerm : searchTerms) {
      addToQuery(delfiSearchObject, searchTerm);
    }
  }

  /**
   * Adds the given query to the already existing one in a lucene notation. Additional queries are
   * added with "and" operation.
   *
   * @param searchTerm search query part to add to the existing one.
   */
  private void addToQuery(DelfiSearchObject delfiSearchObject, String searchTerm) {
    log.debug("Adding to query parameter: {}", searchTerm);
    if (StringUtils.isEmpty(searchTerm)) {
      return;
    }
    delfiSearchObject.setQuery(StringUtils.isEmpty(delfiSearchObject.getQuery()) ? searchTerm
        : delfiSearchObject.getQuery() + LUCENE_AND_TERM + searchTerm);
  }
}
