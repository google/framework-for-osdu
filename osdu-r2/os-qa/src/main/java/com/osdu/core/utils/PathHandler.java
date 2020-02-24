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

package com.osdu.core.utils;

import com.osdu.core.data.parser.JsonParser;
import org.apache.commons.lang3.StringUtils;

public class PathHandler {
    final static String MAIN_PATH = "src/test/resources/data/";

    /**
     * File service
     */
    public static String setFileServiceRequestFilePath(String fileName) {
        return StringUtils.join(MAIN_PATH, "file-service/request/", fileName);
    }


    public static String getTemplate(String fileName) {
        return JsonParser.readJson(StringUtils.join(MAIN_PATH + "workflow/", fileName)).toString();
    }

    /**
     * Mock
     */
    public static String getMockedResponseFilePath(String folder, String fileName) {
        return StringUtils.join(MAIN_PATH + "mock-data/" + folder + "/", fileName);
    }

    /**
     * Ingest
     */
    public static String getJobStatusFunctionPostTemplate(String fileName) {
        return JsonParser.readJson(StringUtils.join(MAIN_PATH + "workflow/", fileName)).toString();
    }

    public static String setIngestRequestFilePath(String fileName) {
        return StringUtils.join(MAIN_PATH, "ingest/request/", fileName);
    }

    public static String setWorkflowRequestFilePath(String fileName) {
        return StringUtils.join(MAIN_PATH, "workflow/request/", fileName);
    }
}