package com.osdu.mapper;

import com.osdu.model.delfi.DelfiSearchObject;
import com.osdu.model.delfi.Sort;
import com.osdu.model.delfi.geo.ByBoundingBox;
import com.osdu.model.delfi.geo.ByDistance;
import com.osdu.model.delfi.geo.ByGeoPolygon;
import com.osdu.model.delfi.geo.GeoType;
import com.osdu.model.delfi.geo.SpatialFilter;
import com.osdu.model.osdu.GeoLocation;
import com.osdu.model.osdu.OsduSearchObject;
import com.osdu.model.osdu.SortOption;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Slf4j
public abstract class SearchObjectMapperDecorator implements SearchObjectMapper {

  static final double DEFAULT_ZERO_DISTANCE = 0.0;
  static final String LUCENE_AND_TERM = " AND ";
  static final String LUCENE_OR_TERM = " OR ";
  static final String BY_BOUNDING_BOX_GEOLOCATION_TYPE = "ByBoundingBox";

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
      result.setSpatialFilter(mapGeoParametersFromGeoLocation(osduSearchObject.getGeoLocation()));
    } else if (osduSearchObject.getGeoCentroid() != null) {
      result.setSpatialFilter(mapGeoParametersFromGeoCentroid(osduSearchObject.getGeoCentroid()));
    }
    result.setSort(mapSort(osduSearchObject.getSort()));
    log.debug("Result of mapping : {}", result);
    return result;
  }

  private Sort mapSort(SortOption[] sortOptions) {
    log.debug("Mapping sort object: {}", sortOptions);
    if (sortOptions != null) {
      Sort sort = osduToDelfiSort(sortOptions);
      log.debug("Result of mapping: {}", sort);
      return sort;
    }
    return null;
  }

  private SpatialFilter mapGeoParametersFromGeoCentroid(List<Double>[] geoCentroidList) {
    log.debug("Mapping geoCentroid object: {}", geoCentroidList);
    SpatialFilter spatialFilter = mapGeoCentroidObject(geoCentroidList);
    log.debug("Result of mapping: {}", spatialFilter);
    return spatialFilter;
  }

  private SpatialFilter mapGeoParametersFromGeoLocation(GeoLocation geoLocation) {
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
              .map(queryKeyValuePair -> queryEntry.getKey() + " : " + "\"" + queryKeyValuePair
                  + "\"")
              .collect(Collectors.joining(LUCENE_OR_TERM))).map(queryTerm -> "(" + queryTerm + ")")
          .collect(Collectors.joining(LUCENE_AND_TERM));
      log.debug("Result of mapping: {}", result);
      return result;
    }
    return null;
  }

  /**
   * Maps sort objects. This is not done via mapstruct since the objects are very different ( they
   * have different structure ) and at the same time they do not have common fields that could at
   * least partially justify the reason for creating a mapper for them.
   *
   * @param sortOptions
   * @return
   */
  private Sort osduToDelfiSort(SortOption[] sortOptions) {
    String[] fields = new String[sortOptions.length];
    String[] orders = new String[sortOptions.length];

    for (int i = 0; i < sortOptions.length; i++) {
      fields[i] = sortOptions[i].getFieldName();
      orders[i] = sortOptions[i].getOrderType().toString().toLowerCase();
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
   * @param geoCentroidList
   * @return
   */
  private SpatialFilter mapGeoCentroidObject(List<Double>[] geoCentroidList) {
    SpatialFilter spatialFilter = new SpatialFilter();

    //there is no direct match between OSDU GeoCentroid and Delfi GeoLocation.
    //but as a fallback measure we decided to infer the value of data from this field in case
    //we don't have data in the GeoLocation OSDU field.
    //1 - One Point is present - this can only be a Point type
    //2 - This is a unique "BoundingBox" type that is not present in RFC for GeoJson
    //3+- This is something else. But given that we know other types that can be used by Delfi Portal
    //    this is the only possible option.
    switch (geoCentroidList.length) {
      case 1:
        spatialFilter.setByDistance(
            new ByDistance(geoCentroidList, DEFAULT_ZERO_DISTANCE));
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
   * @return
   */
  private SpatialFilter mapGeoLocationObject(com.osdu.model.osdu.GeoLocation geoLocation) {
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
                DEFAULT_ZERO_DISTANCE));
        break;
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
