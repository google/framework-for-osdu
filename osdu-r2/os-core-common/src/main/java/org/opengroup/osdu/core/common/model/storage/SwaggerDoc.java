// Copyright 2017-2019, Schlumberger
// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.core.common.model.storage;

public final class SwaggerDoc {

    /*
     * SCHEMA
     */
    public static final String SCHEMA_TAG = "schemas";
    public static final String SCHEMA_DESCRIPTION = "Schema management";

    // CREATE SCHEMA
    public static final String CREATE_SCHEMA = "Create schema";
    public static final String CREATE_SCHEMA_DESCRIPTION = "The API allows the creation of a new schema for the given kind. Required roles: 'users.datalake.editors' or 'users.datalake.admins'.";
    public static final String CREATE_SCHEMA_RESPONSE_CREATED = "Schema created successfully.";
    public static final String CREATE_SCHEMA_RESPONSE_BAD_REQUEST = "Bad request.";
    public static final String CREATE_SCHEMA_RESPONSE_CONFLICT = "Schema already registered.";

    // GET SCHEMA
    public static final String GET_SCHEMA = "Get schema";
    public static final String GET_SCHEMA_DESCRIPTION = "The API returns the schema specified byt the given kind, which must follow the naming convention {Data-Partition-Id}:{dataset}:{type}:{version}. Required roles: 'users.datalake.viewers' or 'users.datalake.editors' or 'users.datalake.admins'.";
    public static final String GET_SCHEMA_RESPONSE_OK = "Schema obtained successfully.";
    public static final String GET_SCHEMA_RESPONSE_BAD_REQUEST = "Bad request.";
    public static final String GET_SCHEMA_RESPONSE_NOT_FOUND = "Schema not found for specified kind.";

    // DELETE SCHEMA
    public static final String DELETE_SCHEMA = "Delete a schema";
    public static final String DELETE_SCHEMA_DESCRIPTION = "The API deletes the schema of the given kind, which must follow the naming convetion {Data-Partition-Id}:{dataset}:{type}:{version} format. This operation cannot be undone. Required roles: 'users.datalake.ops'.";
    public static final String DELETE_SCHEMA_RESPONSE_NO_CONTENT = "Schema deleted successfully.";
    public static final String DELETE_SCHEMA_RESPONSE_BAD_REQUEST = "Bad request.";
    public static final String DELETE_SCHEMA_RESPONSE_NOT_FOUND = "Schema not found.";

    // SCHEMA REQUEST
    public static final String SCHEMA_REQUEST_DESCRIPTION = "Schema definition for the specified record kind.";
    public static final String SCHEMA_REQUEST_KIND = "Record kind for which the schema information is applied to.";
    public static final String SCHEMA_REQUEST_SCHEMA_ITEMS = "List of schema items which compose the schema.";
    public static final String SCHEMA_REQUEST_EXTENSION = "Extension field for custom schema definition.";
    public static final String SCHEMA_REQUEST_SCHEMA_ITEM_DESCRIPTION = "Schema item which describes schema properties and their data types.";
    public static final String SCHEMA_REQUEST_SCHEMA_ITEM_PATH = "Schema item property name.";
    public static final String SCHEMA_REQUEST_SCHEMA_ITEM_KIND = "Schema item property data type.";
    public static final String SCHEMA_REQUEST_SCHEMA_ITEMS_ALLOWED_VALUES = "string, int, float, double, long, boolean, link, datetime";

    /*
     * RECORD
     */

    // CREATE UPDATE RECORD RESPONSE
    public static final String CREATE_UPDATE_RECORD_RESPONSE_DESCRIPTION = "Result of the ingestion request.";
    public static final String CREATE_UPDATE_RECORD_COUNT_DESCRIPTION = "Number of records ingested successfully.";
    public static final String CREATE_UPDATE_RECORD_IDS_DESCRIPTION = "List of ingested record id.";
    public static final String CREATE_UPDATE_RECORD_IDS_SKIPPED = "List of record id that skipped update because it was a duplicate of the existing record.";

    public static final String RECORD_TAG = "records";
    public static final String RECORD_DESCRIPTION = "Records management operations.";
    public static final String RECORD_DATA_DESCRIPTION = "Record payload represented as a list of key-value pairs.";
    public static final String RECORD_ID_DESCRIPTION = "Unique identifier in whole Data Ecosystem. When not provided, Data Ecosystem will create and assign an id to the record. Must follow the naming convention: {Data-Partition-Id}:{object-type}:{uuid}.";
    public static final String RECORD_ID_EXAMPLE = "common:welldb:123456";
    public static final String RECORD_KIND_DESCRIPTION = "Kind of data it is being ingested. Must follow the naming convention: {Data-Partition-Id}:{dataset-name}:{record-type}:{version}.";
    public static final String RECORD_KIND_EXAMPLE = "common:welldb:wellbore:1.0.0";
    public static final String RECORD_ACL_DESCRIPTION = "Group of users who have access to the record.";
    public static final String RECORD_ACL_VIEWER_DESCRIPTION = "List of valid groups which will have view/read privileges over the record.";
    public static final String RECORD_ACL_OWNER_DESCRIPTION = "List of valid groups which will have write privileges over the record.";
    public static final String RECORD_LEGAL_DESCRIPTION = "Attributes which represent the legal constraints associated with the record.";
    public static final String RECORD_LEGAL_TAGS = "List of legaltag names associated with the record. When 'ancestry.parents' are provided, this field is not mandatory.";
    public static final String RECORD_LEGAL_ORDC = "List of other relevant data countries. Must contain the ISO 3166 Alpha-2 code of the country where the data was ingested from. When 'ancestry.parents' are provided, this field is not mandatory.";
    public static final String RECORD_ANCESTRY = "Record ancestry information.";
    public static final String RECORD_ANCESTRY_PARENT = "List of parent records. Must follow the naming convention: {parent-record-id}:{parent-record-version}.";

    // CREATE or UPDATE RECORD
    public static final String CREATE_UPDATE_RECORD = "Create or update records";
    public static final String CREATE_UPDATE_RECORD_DESCRIPTION = "The API represents the main injection mechanism into the Data Ecosystem. It allows records creation and/or update. "
            + "When no record id is provided or when the provided id is not already present in the Data Ecosystem then a new record is created. If the id is related to an existing record "
            + "in the Data Ecosystem then an update operation takes place and a new version of the record is created. Required roles: 'users.datalake.editors' or 'users.datalake.admins'.";

    public static final String CREATE_UPDATE_RECORD_RESPONSE_OK = "Records created and/or updated successfully.";
    public static final String CREATE_UPDATE_RECORD_RESPONSE_BAD_REQUEST = "Invalid record format.";
    public static final String CREATE_UPDATE_RECORD_RESPONSE_NOT_AUTHORIZED = "User not authorized to perform the action.";
    public static final String CREATE_UPDATE_RECORD_RESPONSE_GROUPS_NOT_FOUND = "Invalid acl group.";

    // GET RECORD VERSION
    public static final String GET_RECORD_VERSION = "Get all record versions";
    public static final String GET_RECORD_VERSION_DESCRIPTION = "The API returns a list containing all versions for the given record id. Required roles: 'users.datalake.viewers' or 'users.datalake.editors' or 'users.datalake.admins'.";
    public static final String GET_RECORD_VERSION_RESPONSE_OK = "Record versions retrieved successfully.";
    public static final String GET_RECORD_VERSION_RESPONSE_NOT_FOUND = "Record id or version not found.";

    // PURGE RECORD
    public static final String PURGE_RECORD = "Purge record";
    public static final String PURGE_RECORD_DESCRIPTION = "The API performs the physical deletion of the given record and all of its versions. This operation cannot be undone. Required roles: 'users.datalake.ops'.";
    public static final String PURGE_RECORD_RESPONSE_NO_CONTENT = "Record purged successfully.";
    public static final String PURGE_RECORD_RESPONSE_NOT_FOUND = "Record not found.";

    // DELETE RECORD
    public static final String DELETE_RECORD = "Delete record";
    public static final String DELETE_RECORD_DESCRIPTION = "The API performs a logical deletion of the given record. This operation can be reverted later. Required roles: 'users.datalake.editors' or 'users.datalake.admins'.";
    public static final String DELETE_RECORD_RESPONSE_NO_CONTENT = "Record deleted successfully.";
    public static final String DELETE_RECORD_RESPONSE_NOT_FOUND = "Record not found.";

    // GET LATEST RECORD VERSION
    public static final String GET_LATEST_RECORD_VERSION = "Get record";
    public static final String GET_LATEST_RECORD_VERSION_DESCRIPTION = "This API returns the latest version of the given record. Required roles: 'users.datalake.viewers' or 'users.datalake.editors' or 'users.datalake.admins'.";
    public static final String GET_LATEST_RECORD_VERSION_RESPONSE_OK = "Record retrieved successfully.";
    public static final String GET_LATEST_RECORD_VERSION_RESPONSE_NOT_FOUND = "Record not found.";

    // GET SPECIFIC RECORD VERSION
    public static final String GET_SPECIFIC_RECORD_VERSION = "Get record version";
    public static final String GET_SPECIFIC_RECORD_VERSION_DESCRIPTION = "The API retrieves the specific version of the given record. Required roles: 'users.datalake.viewers' or 'users.datalake.editors' or 'users.datalake.admins'.";
    public static final String GET_SPECIFIC_RECORD_VERSION_RESPONSE_OK = "Record retrieved successfully.";
    public static final String GET_SPECIFIC_RECORD_VERSION_RESPONSE_NOT_FOUND = "Record id or version not found.";

    // RECORD REQUEST
    public static final String RECORD_REQUEST_DESCRIPTION = "Storage record";
    public static final String RECORD_REQUEST_VERSION = "Storage record versions.";
    public static final String RECORD_REQUEST_REPLACERECORD = "Storage Create record.";
    public static final String RECORD_REQUEST_EXPORT = "Storage record export format.";

    /*
     * QUERY
     */
    public static final String QUERY_TAG = "query";
    public static final String QUERY_DESCRIPTION = "Record queries";

    // FETCH RECORD
    public static final String FETCH_RECORDS = "Fetch records";
    public static final String FETCH_RECORD_DESCRIPTION = "The API fetches multiple records at once. Required roles: 'users.datalake.viewers' or 'users.datalake.editors' or 'users.datalake.admins'.";
    public static final String FETCH_RECORD_RESPONSE_OK = "Fetch multiple records successfully.";
    public static final String FETCH_RECORD_RESPONSE_BAD_REQUEST = "Bad Reqeust. please provide valid headers and request no more than 20 records.";
    public static final String FETCH_RECORD_RESPONSE_SERVER_ERROR = "Unknown storage error.";
    public static final String FETCH_RECORD_ID_LIST = "List of record ids. Each record id must follow the naming convention {Data-Partition-Id}:{dataset-name}:{record-type}:{version}.";

    // FETCH RECORD V2
    public static final String FETCH_RECORDS_V2 = "Fetch records with optional conversion";
    public static final String FETCH_RECORD_DESCRIPTION_V2 = "Fetch records and do corresponding conversion as user requested, no more than 20 records per request.";
    public static final String FETCH_RECORD_RESPONSE_OK_V2 = "Fetch multiple records with optional conversion successfully.";
    public static final String FETCH_RECORD_RESPONSE_BAD_REQUEST_V2 = "Bad Reqeust. please make sure to provide valid headers and have no more than 20 records per request.";
    public static final String FETCH_RECORD_RESPONSE_SERVER_ERROR_V2 = "Unknown storage error in fetch record v2.";
    public static final String FETCH_RECORD_ID_LIST_V2 = "List of record ids. Each record id must follow the naming convention as {Data-Partition-Id}:{dataset-name}:{record-type}:{version}.";

    // GET KINDS
    public static final String GET_KINDS = "Get all kinds";
    public static final String GET_KINDS_DESCRIPTION = "The API returns a list of all kinds in the specific {Data-Partition-Id}. Required roles: 'users.datalake.editors' or 'users.datalake.admins'.";
    public static final String GET_KINDS_RESPONSE_OK = "All kinds retrieved successfully.";
    public static final String GET_KINDS_RESPONSE_SERVER_ERROR = "Unknown Error.";

    // GET ALL RECORD
    public static final String GET_ALL_RECORD = "Get all record from kind";
    public static final String GET_ALL_RECORD_DESCRIPTION = "The API returns a list of all record ids which belong to the specified kind. Required roles: 'users.datalake.ops'.";
    public static final String GET_ALL_RECORD_RESPONSE_OK = "Record Ids retrieved successfully.";
    public static final String GET_ALL_RECORD_RESPONSE_NOT_FOUND = "Kind or cursor not found.";

    // QUERY REQUEST
    public static final String QUERY_REQUEST_MULTIPLE_RECORD_ID = "Multiple storage record ids and filter attributes.";
    public static final String QUERY_REQUEST_MULTIPLE_RECORD_INFO = "Multiple Storage records.";

    // PARAMETERS
    public static final String PARAMETER_EMAIL = "User email";
    public static final String PARAMETER_KIND = "Kind";
    public static final String PARAMETER_FILTER_KIND = "Filter Kind";
    public static final String PARAMETER_CORRELATION_ID = "Correlation ID";
    public static final String PARAMETER_SKIP_DUPLICATE = "Skip duplicates when updating records with the same value.";
    public static final String PARAMETER_RECORD_ID = "Record id";
    public static final String PARAMETER_ATTRIBUTES = "Filter attributes to restrict the returned fields of the record. Usage: data.{record-data-field-name}.";
    public static final String PARAMETER_ATTRIBUTES_EXAMPLE = "data.wellName";
    public static final String PARAMETER_VERSION = "Record version";
    public static final String PARAMETER_VERSION_EXAMPLE = "123456789";
    public static final String PARAMETER_CURSOR = "Cursor";
    public static final String PARAMETER_LIMIT = "Page Size";

    // HEADERS
    public static final String PARTITION_ID = "This value should be the desired data partition id.";

    public static final String PARAM_TYPE = "header";
    public static final String PARAM_DATA_TYPE = "string";
    public static final String PARAM_DEFAULT_VALUE = "common";

    private SwaggerDoc() {
        // private constructor
    }
}
