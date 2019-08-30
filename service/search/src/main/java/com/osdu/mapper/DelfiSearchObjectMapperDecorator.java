package com.osdu.mapper;

import com.osdu.model.delfi.DelfiSearchObject;
import com.osdu.model.delfi.geo.*;
import com.osdu.model.delfi.Sort;
import com.osdu.model.delfi.geo.exception.GeoLocationException;
import com.osdu.model.osdu.OSDUSearchObject;
import com.osdu.model.osdu.SortOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;


public abstract class DelfiSearchObjectMapperDecorator implements SearchObjectMapper {

    private final static Logger log = LoggerFactory.getLogger(DelfiSearchObjectMapperDecorator.class);

    private static final double DEFAULT_ZERO_DISTANCE = 0.0;
    private static final String LUCENE_AND_TERM = " AND ";
    private static final String LUCENE_OR_TERM = " OR ";

    @Inject
    @Named("com.osdu.mapper.SearchObjectMapperImpl_")
    private SearchObjectMapper delegate;

    @Override
    public DelfiSearchObject osduSearchObjectToDelfiSearchObject(OSDUSearchObject osduSearchObject, String kind, String partition) throws GeoLocationException {
        log.debug("Mapping request for object : {}", osduSearchObject);
        DelfiSearchObject result = delegate.osduSearchObjectToDelfiSearchObject(osduSearchObject, kind, partition);
        addToQuery(result, osduSearchObject.getFulltext(), mapMetadata(osduSearchObject));
        result.setKind(kind);
        mapGeoParameters(osduSearchObject, result);
        log.debug("Result of mapping : {}", result);
        return result;
    }

    private void mapGeoParameters(OSDUSearchObject osduSearchObject, DelfiSearchObject result) throws GeoLocationException {
        if (osduSearchObject.getGeoLocation() != null) {
            log.debug("Mapping geoLocation object: {}", osduSearchObject.getGeoLocation());
            SpatialFilter spatialFilter = mapGeoLocationObject(osduSearchObject);
            result.setSpatialFilter(spatialFilter);
            log.debug("Result of mapping: {}", spatialFilter);
        }

        if (osduSearchObject.getGeoLocation() == null && osduSearchObject.getGeoCentroid() != null) {
            log.debug("Mapping geoCentroid object: {}", osduSearchObject.getGeoCentroid());
            SpatialFilter spatialFilter = mapGeoCentroidObject(osduSearchObject);
            result.setSpatialFilter(spatialFilter);
            log.debug("Result of mapping: {}", spatialFilter);
        }

        if (osduSearchObject.getSort() != null) {
            log.debug("Mapping sort object: {}", osduSearchObject.getSort());
            Sort sort = mapSort(osduSearchObject);
            result.setSort(sort);
            log.debug("Result of mapping: {}", sort);
        }
    }

    private String mapMetadata(OSDUSearchObject osduSearchObject) {
        log.debug("Mapping metadata object: {}", osduSearchObject.getMetadata());
        if (osduSearchObject.getMetadata() != null) {
            StringBuilder stringBuilder = new StringBuilder();
            for (Map.Entry<String, Object> metadataEntry : osduSearchObject.getMetadata().entrySet()) {
                createQueryEntry(stringBuilder, metadataEntry.getKey(), metadataEntry.getValue());
            }
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            String result = stringBuilder.toString();
            log.debug("Result of mapping: {}", result);
            return result;
        }
        return null;
    }

    private void createQueryEntry(StringBuilder stringBuilder, String key, Object value) {
        if (value instanceof List) {
            for (Object o : (List) value) {
                createQueryEntry(stringBuilder, key, o);
                stringBuilder.append(LUCENE_OR_TERM);
            }
            stringBuilder.delete(stringBuilder.lastIndexOf(LUCENE_OR_TERM), stringBuilder.length() - 1);
        } else {
            stringBuilder.append(key);
            stringBuilder.append(":");
            stringBuilder.append("\"").append(value).append("\"");
            stringBuilder.append(",");
        }
    }

    /**
     * Maps sort objects. This is not done via mapstruct since the objects are very different ( they have different structure ) and at the same time
     * they do not have common fields that could at least partially justify the reason for creating a mapper for them.
     *
     * @param osduSearchObject
     * @return
     */
    private Sort mapSort(OSDUSearchObject osduSearchObject) {
        List<String> fields = new ArrayList<>();
        List<String> orders = new ArrayList<>();

        for (SortOption sortOption : osduSearchObject.getSort()) {
            fields.add(sortOption.getFieldName());
            orders.add(sortOption.getOrderType().toString().toLowerCase());
        }

        Sort sort = new Sort();
        sort.setField(new String[fields.size()]);
        sort.setOrder(new String[orders.size()]);
        fields.toArray(sort.getField());
        orders.toArray(sort.getOrder());
        return sort;
    }

    /**
     * Map GeoCentroid. Not mapped via mapstruct for same reasons as geoLocation, but in this case mapping of this field is
     * optional and based on the mapping of the geoLocation object
     *
     * @param osduSearchObject
     * @return
     * @throws GeoLocationException
     */
    private SpatialFilter mapGeoCentroidObject(OSDUSearchObject osduSearchObject) throws GeoLocationException {
        SpatialFilter spatialFilter = new SpatialFilter();

        //there is no direct match between OSDU GeoCentroid and Delfi GeoLocation.
        //but as a fallback measure we decided to infer the value of data from this field in case
        //we don't have data in the GeoLocation OSDU field.
        //1 - One Point is present - this can only be a Point type
        //2 - This is a unique "BoundingBox" type that is not present in RFC for GeoJson
        //3+- This is something else. But given that we know other types that can be used by Delfi Portal
        //    this is the only possible option.
        switch (osduSearchObject.getGeoCentroid().length) {
            case 1:
                spatialFilter.setByDistance(new ByDistance(osduSearchObject.getGeoCentroid(), DEFAULT_ZERO_DISTANCE));
                break;
            case 2:
                spatialFilter.setByBoundingBox(new ByBoundingBox(osduSearchObject.getGeoCentroid()));
                break;
            default:
                spatialFilter.setByGeoPolygon(new ByGeoPolygon(osduSearchObject.getGeoCentroid()));
        }
        return spatialFilter;
    }

    /**
     * Manually map sort objects. They are not extracted into mapstruct mappers since they are quite small but have completely
     * different format.
     *
     * @param osduSearchObject
     * @return
     * @throws GeoLocationException
     */
    private SpatialFilter mapGeoLocationObject(OSDUSearchObject osduSearchObject) throws GeoLocationException {
        SpatialFilter spatialFilter = new SpatialFilter();
        switch (GeoType.lookup(osduSearchObject.getGeoLocation().getType())) {
            case BY_BOUNDING_BOX:
                spatialFilter.setByBoundingBox(new ByBoundingBox(osduSearchObject.getGeoLocation().getCoordinates()));
                break;
            case BY_DISTANCE:
                spatialFilter.setByDistance(new ByDistance(osduSearchObject.getGeoLocation().getCoordinates(), osduSearchObject.getGeoLocation().getDistance()));
                break;
            case POLYGON:
            case BY_GEO_POLYGON:
                spatialFilter.setByGeoPolygon(new ByGeoPolygon(osduSearchObject.getGeoLocation().getCoordinates()));
                break;
            case POINT:
                spatialFilter.setByDistance(new ByDistance(osduSearchObject.getGeoLocation().getCoordinates(), DEFAULT_ZERO_DISTANCE));
                break;
        }
        if (osduSearchObject.getGeoLocation().getType().equals("ByBoundingBox")) {
            spatialFilter.setByBoundingBox(new ByBoundingBox(osduSearchObject.getGeoLocation().getCoordinates()));
        }
        return spatialFilter;
    }

    private void addToQuery(DelfiSearchObject delfiSearchObject, String... searchTerms) {
        for (String searchTerm : searchTerms) {
            addToQuery(delfiSearchObject, searchTerm);
        }
    }

    /**
     * Adds the given query to the already existing one in a lucene notation. Additional queries are added with
     * "and" operation.
     *
     * @param searchTerm search query part to add to the existing one.
     */
    private void addToQuery(DelfiSearchObject delfiSearchObject, String searchTerm) {
        log.debug("Adding to query parameter: {}", searchTerm);
        if (StringUtils.isEmpty(searchTerm)) {
            return;
        }

        if (StringUtils.isEmpty(delfiSearchObject.getQuery())) {
            delfiSearchObject.setQuery(searchTerm);
        } else {
            //TODO: Check that we match with Lucene syntax
            delfiSearchObject.setQuery(delfiSearchObject.getQuery() + LUCENE_AND_TERM + searchTerm);
        }
    }
}
