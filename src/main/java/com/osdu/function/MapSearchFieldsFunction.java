package com.osdu.function;

import com.osdu.model.delfi.geo.ByBoundingBox;
import com.osdu.model.delfi.DelfiSearchObject;
import com.osdu.model.delfi.Point;
import com.osdu.model.delfi.SpatialFilter;
import com.osdu.model.osdu.GeoLocation;
import com.osdu.model.osdu.OSDUSearchObject;
import com.osdu.service.DelfiSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.function.Function;

/**
 * Function to Map OSDU compliant search query to Delfi query.
 * Input format is described in "SDU-82935841-250319-1033.pdf", output format is taken from API description from
 * Delfi Developer Portal -> Search Service -> /query
 */
@Component
public class MapSearchFieldsFunction implements Function<Message<OSDUSearchObject>, Message<String>> {

    private final static Logger log = LoggerFactory.getLogger(MapSearchFieldsFunction.class);

    private static final String KIND_WILDCARD = ":*:*:*";

    @Value("${search.mapper.delfi.partition}")
    private String partition;

    @Autowired
    private DelfiSearchService delfiSearchService;

    @Override
    public Message<String> apply(Message<OSDUSearchObject> messageSource) {
        OSDUSearchObject source = messageSource.getPayload();
        log.info("Received request to search with following params : {}", source);

        DelfiSearchObject result = new DelfiSearchObject();
        if (source.getCount() != null) {
            result.setLimit(source.getCount());
        }
        if (source.getStart() != null) {
            result.setOffset(source.getStart());
        }
        if (source.getFulltext() != null) {
            result.addToQuery(source.getFulltext());
        }
        if (source.getMetadata() != null) {
            StringBuilder stringBuilder = new StringBuilder();
            for (Object key : ((LinkedHashMap) source.getMetadata()).keySet()) {
                stringBuilder.append(mapJSONToDelfiQuery((String) key, ((LinkedHashMap) source.getMetadata()).get(key))).append(",");
            }
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            result.addToQuery(stringBuilder.toString());
        }

        if (source.getFacets() != null) {
            result.setReturnedFields(source.getFacets());
        }

        result.setKind(partition + KIND_WILDCARD);

        if (source.getGeoLocation() != null) {
            SpatialFilter spatialFilter = new SpatialFilter();
            spatialFilter.setType(source.getGeoLocation().getType());
            //TODO: get possible GeoJSON types from RFC and implement support for at least types available in Delfi ( at least 1 more type expected )
            if (source.getGeoLocation().getType().equals(GeoLocation.BY_BOUNDING_BOX_GEO_TYPE)) {
                Double[] coordinates = source.getGeoLocation().getCooridanates();
                spatialFilter.setByBoundingBox(new ByBoundingBox(new Point(coordinates[0], coordinates[1]), new Point(coordinates[2], coordinates[3])));
            }
            result.setSpatialFilter(spatialFilter);
        }

        if (source.getSort() != null) {
            //TODO: implement
        }
        String resultingString = delfiSearchService.searchIndex(result, messageSource.getHeaders());
        log.info("Found search result : {}", resultingString);
        return new GenericMessage<>(resultingString, Collections.singletonMap("Content-Type", "application/json;charset=UTF-8"));
    }

    /**
     * Maps JSON with inner entities to key:value escaped format with key being the path to the value.
     *
     * @param key   key to current level of inner object. Will be basically a path to lowest level.
     * @param value value of the given property
     * @return string for current field in Query format supported by Delfi
     */
    private String mapJSONToDelfiQuery(String key, Object value) {
        String result = key;
        if (value instanceof LinkedHashMap) {
            for (Object o : ((LinkedHashMap) value).keySet()) {
                // assuming that there is only 1 lowest-level entry in each object for now (i.e. there are no real complex objects inside metadata field )
                // TODO: Add support for complex objects
                return mapJSONToDelfiQuery(result + "." + o, ((LinkedHashMap) value).get(o));
            }
        } else {
            //escaped quotes needed since resulting string will be : "query": key:\"value\"
            return result + ":\"" + value + "\"";
        }
        return null;
    }
}
