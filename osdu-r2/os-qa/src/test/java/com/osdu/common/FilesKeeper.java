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

package com.osdu.common;

import com.osdu.core.utils.PathHandler;

public class FilesKeeper {
    /**
     * File service paths
     */
    public static String requestFileServicePath = PathHandler.setFileServiceRequestFilePath("requestTemplate.json");
    public static String requestWithMismathchedRequestToFileServicePath = PathHandler.setFileServiceRequestFilePath("getLocationWithInteger.json");
    public static String requestWithLongIdPath = PathHandler.setFileServiceRequestFilePath("longFileId.json");
    public static String requestWithInvalidJsonPath = PathHandler.setFileServiceRequestFilePath("invalidJson.json");
    public static String requestTemplateForFiles = PathHandler.setFileServiceRequestFilePath("filesListRequestTemplate.json");
    public static String requestForFilesWithTime = PathHandler.setFileServiceRequestFilePath("filesListRequestWithTime.json");
    public static String requestForFilesWithoutTime = PathHandler.setFileServiceRequestFilePath("filesListRequestWithoutTime.json");
    public static String requestForFilesWithoutItems = PathHandler.setFileServiceRequestFilePath("filesListRequestWithoutItems.json");
    public static String requestForFilesWithPageNumFieldMismatch = PathHandler.setFileServiceRequestFilePath("filesListRequestWithPageNumFieldMismatch.json");
    public static String requestForFilesWithItemsFieldMismatch = PathHandler.setFileServiceRequestFilePath("filesListRequestWithItemsFieldMismatch.json");
    public static String requestForFilesWithUserIdFieldMismatch = PathHandler.setFileServiceRequestFilePath("filesListRequestWithUserIdFieldMismatch.json");
    public static String requestForFilesWithInvalidData = PathHandler.setFileServiceRequestFilePath("filesListRequestWithInvalidDataFormat.json");
    public static String requestForFilesWithWrongTimeRange = PathHandler.setFileServiceRequestFilePath("filesListRequestWithWrongTimeRange.json");
    public static String requestForFilesWithNotExistedTime = PathHandler.setFileServiceRequestFilePath("filesListRequestWithNotExistedTime.json");
    public static String requestForFilesWithNegativeItemsNumber = PathHandler.setFileServiceRequestFilePath("filesListRequestWithNegativeItemsNumber.json");
    public static String requestForFilesWithHugePageNumValue = PathHandler.setFileServiceRequestFilePath("filesListRequestWithHugePageNumValue.json");
    public static String requestForFilesWithoutDataTime = PathHandler.setFileServiceRequestFilePath("filesListRequestWithoutDataTime.json");
    public static String requestForFilesWithInvalidJson = PathHandler.setFileServiceRequestFilePath("filesListRequestWithInvalidJson.json");

    /**
     * Ingest service paths
     */
    public static String requestForIngestTemplate = PathHandler.setIngestRequestFilePath("ingestTemplate.json");
    public static String requestForIngestWithManifest = PathHandler.setIngestRequestFilePath("validManifest.json");
    public static String requestForIngestWithInvalidManifest = PathHandler.setIngestRequestFilePath("manifestWithoutWorkProduct.json");
    public static String requestForIngestWithoutFileId = PathHandler.setIngestRequestFilePath("ingestRequestWithoutFileId.json");
    public static String requestForIngestWithmismathedDataTypeValue = PathHandler.setIngestRequestFilePath("ingestRequestWithMismatchedDataTypeValue.json");

    /**
     * Workflow service paths
     */
    public static String requestForWorkflowStatusTemplate = PathHandler.setWorkflowRequestFilePath("workflowTemplate.json");
    public static String requestForStartWorkflowTemplate = PathHandler.setWorkflowRequestFilePath("startWorkflowTemplate.json");
    public static String requestForStartWorkflowWithoutContext = PathHandler.setWorkflowRequestFilePath("startWorkflowWithoutContext.json");
    public static String requestForStartWorkflowWithMismatchedvalues = PathHandler.setWorkflowRequestFilePath("startWorkflowWithMismatchedValues.json");
    public static String requestForWorkflowUpdateStatusTemplate = PathHandler.setWorkflowRequestFilePath("updateWorkflowTemplate.json");
    public static String requestForWorkflowUpdateStatusWithoutId = PathHandler.setWorkflowRequestFilePath("updateWorkflowWithoutWorkflowId.json");
    public static String requestForWorkflowUpdateStatusWithLongId = PathHandler.setWorkflowRequestFilePath("updateWorkflowWithLongWorkflowId.json");
    public static String requestForWorkflowUpdateStatusWithMismatchedValue = PathHandler.setWorkflowRequestFilePath("updateWorkflowWithMismatchedValue.json");
}