/*
 * Copyright 2020 Google LLC
 * Copyright 2017-2019, Schlumberger
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

package org.opengroup.osdu.core.common;

public final class SwaggerDoc {

    // GENERAL
    public static final String RESPONSE_BAD_GATEWAY = "Search service scale-up is taking longer than expected. Wait 10 seconds and retry.";

    // SEARCH
    public static final String SEARCH_TAG = "Search";
    public static final String SEARCH_DESCRIPTION = "Service endpoints to search data in Data Ecosystem";

    // QUERY
    public static final String QUERY_POST_TITLE = "Queries using the input request criteria.";
    public static final String QUERY_OPERATION_ID = "Query";
    public static final String QUERY_POST_NOTES = "The API supports full text search on string fields, range queries on date, numeric or string fields, along with geo-spatial search. Required roles: 'users.datalake.viewers' or 'users.datalake.editors' or 'users.datalake.admins' or 'users.datalake.ops'. In addition, users must be a member of data groups to access the data.";
    public static final String QUERY_POST_RESPONSE_OK = "Success";
    public static final String QUERY_POST_RESPONSE_BAD_REQUEST = "Invalid parameters were given on request";
    public static final String QUERY_POST_RESPONSE_NOT_AUTHORIZED = "User not authorized to perform the action";

    // CCS QUERY
    public static final String CCS_QUERY_OPERATION_ID = "CCS Query";
    public static final String CCS_QUERY_NOTES = "The API supports cross cluster searches when given the list of partitions.";

    // QUERY_WITH_CURSOR
    public static final String QUERY_WITH_CURSOR_POST_TITLE = "Queries using the input request criteria.";
    public static final String QUERY_WITH_CURSOR_OPERATION_ID = "Query with cursor";
    public static final String QUERY_WITH_CURSOR_POST_NOTES = "The API supports full text search on string fields, range queries on date, numeric or string fields, along with geo-spatial search. Required roles: 'users.datalake.viewers' or 'users.datalake.editors' or 'users.datalake.admins' or 'users.datalake.ops'. In addition, users must be a member of data groups to access the data. It can be used to retrieve large numbers of results (or even all results) from a single search request, in much the same way as you would use a cursor on a traditional database.";
    public static final String QUERY_WITH_CURSOR_POST_RESPONSE_OK = "Success";
    public static final String QUERY_WITH_CURSOR_POST_RESPONSE_BAD_REQUEST = "Invalid parameters were given on request";
    public static final String QUERY_WITH_CURSOR_POST_RESPONSE_NOT_AUTHORIZED = "User not authorized to perform the action";

    // SCHEMA
    public static final String INDEX_SCHEMA_GET_TITLE = "Returns the index schema for given 'kind'.";
    public static final String INDEX_SCHEMA_OPERATION_ID = "Get index schema";
    public static final String INDEX_SCHEMA_GET_NOTES = "The API returns the schema for a given kind which is used find what attributes are indexed and their respective data types (at index time). Required roles: 'users.datalake.viewers' or 'users.datalake.editors' or 'users.datalake.admins' or 'users.datalake.ops'";
    public static final String INDEX_SCHEMA_GET_RESPONSE_OK = "Success";
    public static final String INDEX_SCHEMA_GET_RESPONSE_BAD_REQUEST = "Invalid parameters were given on request";
    public static final String INDEX_SCHEMA_GET_RESPONSE_NOT_FOUND = "Index schema for requested kind not found";

    // DELETE INDEX
    public static final String INDEX_DELETE_TITLE = "Deletes all documents from index for given 'kind'.";
    public static final String INDEX_DELETE_OPERATION_ID = "Delete index";
    public static final String INDEX_DELETE_NOTES = "The API can be used to purge all indexed documents for a kind. Required roles: 'users.datalake.admins' or 'users.datalake.ops'";
    public static final String INDEX_DELETE_RESPONSE_NO_CONTENT = "No Content Returned";
    public static final String INDEX_DELETE_RESPONSE_BAD_REQUEST = "Invalid parameters were given on request";
    public static final String INDEX_DELETE_RESPONSE_NOT_FOUND = "Requested kind not found";
    public static final String INDEX_DELETE_RESPONSE_CONFLICT = "Unable to delete the index because it is currently locked";

    // SEARCH SERVICE PARAMETERS
    public static final String PARAMETER_KIND = "Kind of the record.";
    public static final String PARAMETER_ACCOUNT_ID = "Account ID is the active DELFI account (SLB account or customer's account) which the users choose to use with the Search API.";
    public static final String PARAMETER_ONBEHALF_ACCOUNT_ID = "Token (Google ID or SAuth) representing the user whose request is sent on behalf of.";

    // REQUEST, RESPONSE & VALIDATIONS
    // QUERIES
    public static final String KIND_REQUEST_DESCRIPTION = "'kind' to search";
    public static final String KIND_VALIDATION_CAN_NOT_BE_NULL_OR_EMPTY = "'kind' can not be null or empty";
    public static final String KIND_EXAMPLE = "common:ihs:well:1.0.0";
    public static final String LIMIT_VALIDATION_MIN_MSG = "'limit' must be equal or greater than 0";
    public static final String OFFSET_VALIDATION_MIN_MSG = "'offset' must be equal or greater than 0";
    public static final String SORT_FIELD_VALIDATION_NOT_EMPTY_MSG = "'sort.field' can not be null or empty";
    public static final String SORT_FIELD_LIST_VALIDATION_NOT_EMPTY_MSG = "'sort.field' list can not have null or empty values";
    public static final String SORT_NOT_VALID_ORDER_OPTION = "Not a valid order option. It can only be either 'ASC' or 'DESC'";
    public static final String SORT_ORDER_VALIDATION_NOT_EMPTY_MSG = "'sort.order' can not be null or empty";
    public static final String SORT_FIELD_ORDER_SIZE_NOT_MATCH = "'sort.field' and 'sort.order' size do not match";
    public static final String LIMIT_DESCRIPTION = "The maximum number of results to return from the given offset. If no limit is provided, then it will return 10 items. Max number of items which can be fetched by the query is 100. (If you wish to fetch large set of items, please use query_with_cursor API)";
    public static final String QUERY_DESCRIPTION = "The query string in Lucene query string syntax.";
    public static final String SORT_DESCRIPTION = "The fields and orders to return sorted results.";
    public static final String SORT_FIELD_DESCRIPTION = "The list of fields to sort the results.";
    public static final String SORT_ORDER_DESCRIPTION = "The list of orders to sort the results. The element must be either ASC or DESC.";
    public static final String RETURNED_FIELDS_DESCRIPTION = "The fields on which to project the results.";
    public static final String OFFSET_DESCRIPTION = "The starting offset from which to return results.";
    public static final String CURSOR_DESCRIPTION = "Search context to retrieve next batch of results.";
    public static final String AGGREGATEBY_DESCRIPTION = "The aggregateBy field returns the distinct values of the given field.";
    public static final String QUERYASOWNER_DESCRIPTION = "The queryAsOwner switches between viewer and owner to return results that you are entitled to view or results you are the owner of.";

    // SPATIAL FILTER
    public static final String FIELD_VALIDATION_NON_NULL_MSG = "'spatialFilter.field' can not be null";
    public static final String TOPLEFT_VALIDATION_NON_NULL_MSG = "'byBoundingBox.topLeft' can not be null";
    public static final String BOTTOMRIGHT_VALIDATION_NON_NULL_MSG = "'byBoundingBox.bottomRight' can not be null";
    public static final String DISTANCE_VALIDATION_MIN_MSG = "'distance' must be greater than 0";
    public static final String DISTANCE_VALIDATION_MAX_MSG = "'distance' cannot be greater than 1.5E203";
    public static final String DISTANCE_POINT_VALIDATION_NON_NULL_MSG = "'byDistance.coordinate' can not be null";
    public static final String GEOPOLYGON_POINT_VALIDATION_NON_NULL_MSG = "'byGeoPolygon.point' list can not be null or empty";
    public static final String LATITUDE_VALIDATION_RANGE_MSG = "'latitude' value is out of the range [-90, 90]";
    public static final String LONGITUDE_VALIDATION_RANGE_MSG = "'longitude' value is out of the range [-180, 180]";
    public static final String FIELD_DESCRIPTION = "geo-point field in the index on which filtering will be performed. Use GET schema API to find which fields supports spatial search.";
    public static final String SPATIAL_FILTER_DESCRIPTION = "A spatial filter to apply.";
    public static final String QUERY_BY_BOUNDING_BOX_DESCRIPTION = "A query allowing to filter hits based on a point location within a bounding box.";
    public static final String QUERY_BY_DISTANCE_DESCRIPTION = "Filters documents that include only hits that exist within a specific distance from a geo point.";
    public static final String QUERY_BY_GEO_POLYGON_DESCRIPTION = "A query allowing to filter hits that only fall within a polygon of points.";
    public static final String TOPLEFT_BOUNDING_BOX_DESCRIPTION = "Top left corner of the bounding box.";
    public static final String BOTTOMRIGHT_BOUNDING_BOX_DESCRIPTION = "Bottom right corner of the bounding box.";
    public static final String DISTANCE_DESCRIPTION = "The radius of the circle centered on the specified location. Points which fall into this circle are considered to be matches.";
    public static final String POINT_DISTANCE_DESCRIPTION = "Center point of the query.";
    public static final String POINTS_GEO_POLYGON_DESCRIPTION = "Polygon defined by a set of points.";
    public static final String LATITUDE = "Latitude of point.";
    public static final String LONGITUDE = "Longitude of point.";

    // INDEXER
    public static final String INDEXER_TAG = "Indexer";
    public static final String INDEXER_DESCRIPTION = "Indexer endpoints to index data in Data Ecosystem";

    // REINDEX
    public static final String REINDEX_POST_TITLE = "Re-index given 'kind'.";
    public static final String REINDEX_OPERATION_ID = "Reindex kind";
    public static final String REINDEX_POST_NOTES = "The API triggers re-indexing of 'kind'. Required roles: 'users.datalake.admins' or 'users.datalake.ops'";
    public static final String REINDEX_POST_RESPONSE_OK = "Success";
    public static final String REINDEX_POST_RESPONSE_BAD_REQUEST = "Invalid parameters were given on request";
    public static final String REINDEX_POST_RESPONSE_NOT_FOUND = "Requested 'kind' not found";

    // TASK STATUS
    public static final String COPY_TASK_GET_TITLE = "Get status of task running for the tenant.";
    public static final String COPY_TASK_OPERATION_ID = "Task status";
    public static final String COPY_TASK_GET_NOTES = "Get status of task running for the tenant. Required roles: 'users.datalake.admins' or 'users.datalake.ops'";
    public static final String COPY_TASK_GET_RESPONSE_OK = "Success";

    // COPY INDEX
    public static final String COPY_INDEX_GET_TITLE = "Copies index for kind from 'common' tenant to private tenant.";
    public static final String COPY_INDEX_OPERATION_ID = "Copy index";
    public static final String COPY_INDEX_POST_NOTES = "Copies index for kind from 'common' tenant to private tenant. Required roles: 'users.datalake.admins' or 'users.datalake.ops'";
    public static final String COPY_INDEX_POST_RESPONSE_OK = "Success";

    // INDEXER SERVICE PARAMETERS
    public static final String PARAMETER_TASK_ID = "Task id.";

    // SHARED AUTH
    public static final String BEARER_AUTH = "Bearer";
    public static final String GOOGLE_ID_AUTH = "google_id_token";
    public static final String SAUTH_ID_AUTH = "sauth_id_token";

    // REQUEST VALIDATION
    public static final String REQUEST_VALIDATION_NOT_NULL_BODY = "Request body can not be null";
}
