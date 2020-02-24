/*
 * Copyright  2019 Google LLC
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

package com.osdu.core.data.provider;

public class TestData {
    /**
     * Data keys for data provider
     */
    public static final String DRIVER = "Driver";
    public static final String LOCATION = "Location";
    public static final String USER_ID = "UserID";
    public static final String EMPTY_REQUEST = "emptyRequest";
    public static final String CONTENT_TYPE = "contentType";
    public static final String WORKFLOW_TYPE_INGEST = "workflowTypeIngest";
    public static final String WORKFLOW_TYPE_OSDU = "workflowTypeOsdu";

    /**
     * Error msg's
     */
    public static final String ERROR_TYPE_MISMATCH = "errorMismatch";
    public static final String EXCEPTION = "exception";
    public static final String ERROR_TIME_PARSING = "errorTimeParsing";
    public static final String ERROR_JSON_PARSING = "errorJsonParsing";
    public static final String ERROR_CONSTRAINT_VIOLATION = "errorViolation";
    public static final String ERROR_INVALID_FORMAT = "errorInvalidFormat";
    /**
     * Response keys for ingest
     */
    public static final String STATUS = "Status";
    public static final String STATUS_RUNNING = "status_running";
    public static final String STATUS_FINISHED = "status_finished";
    public static final String STATUS_FAILED = "status_failed";
    public static final String DATA_TYPE_LOG = "dataTypeLog";
    public static final String DATA_TYPE_OSDU = "dataTypeOsdu";
    public static final String DATA_TYPE_OPAQUE = "dataTypeOpaque";
    public static final String DATA_TYPE_INVALID = "dataTypeInvalid";

    /**
     * Response paths for file service
     */
    public static final String FILE_ID = "FileID";
    public static final String SIGNED_URL = "Location.SignedURL";
    public static final String GET_LOCATION_FROM_FILES = "Сontent[0].Location";
    public static final String GET_FILE_ID_FROM_FILES = "Сontent[0].FileID";
    public static final String GET_CREATION_TIME_FROM_FILES = "CreatedAt";
    public static final String GET_CREATOR_FROM_FILES = "CreatedBy";
    public static final String CONTENT = "Content";
    public static final String MESSAGE = "message";

    /**
     * Response paths for airflow
     */
    public static final String AIRFLOW_ITEMS = "items";
    public static final String AIRFLOW_DAG_ID = "dag_id";
    public static final String AIRFLOW_DAG_ID_DEFAULT = "dag_id_default";
    public static final String AIRFLOW_DAG_ID_OSDU = "dag_id_osdu";
    public static final String AIRFLOW_DAG_RUN_URL = "dag_run_url";
    public static final String AIRFLOW_EXECUTION_TIME = "execution_date";


    /**
     * Response paths for ingest service
     */
    public static final String WORKFLOW_ID = "WorkflowID";
}