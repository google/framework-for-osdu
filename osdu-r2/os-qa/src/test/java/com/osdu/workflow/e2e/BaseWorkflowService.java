/*
 * Copyright  2020 Google LLC
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

package com.osdu.workflow.e2e;

import com.osdu.auth.Authentication;
import com.osdu.core.data.properties.PropertyHolder;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.util.HashMap;

import static com.osdu.core.utils.constants.RequestConstantHolder.*;
import static com.osdu.utils.EnvironmentVariableReceiver.getBearerToken;

public class BaseWorkflowService extends Authentication {
    public RequestSpecification baseRequestSpec(HashMap<String, String> headers) {
        return new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .addHeaders(headers)
                .build();
    }

    public HashMap<String, String> specifiedHeadersSet() {
        return new HashMap<String, String>() {{
            put(ACCEPT, ACCEPT_VALUE);
            put(PARTITION_ID, PropertyHolder.headers.getPartitionId());
            put(AUTHORIZATION, BEARER + getBearerToken());
        }};
    }
}