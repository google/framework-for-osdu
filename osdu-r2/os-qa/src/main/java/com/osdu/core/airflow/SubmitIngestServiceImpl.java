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

package com.osdu.core.airflow;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.osdu.core.data.properties.PropertyHolder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmitIngestServiceImpl {
    static GoogleIapHelper googleIapHelper = new GoogleIapHelper();

    @SneakyThrows
    public static JSONObject submitIngest() {
        String airflowUrl = PropertyHolder.remoteProps.getAirflowApi();
        String iapClientId = googleIapHelper.getIapClientId(airflowUrl);
        String webServerUrl = airflowUrl + PropertyHolder.remoteProps.getAirflowDagsList();
        HttpRequest request = googleIapHelper.buildIapRequest(webServerUrl, iapClientId);
        HttpResponse response = request.execute();

        JSONParser jsonParser = new JSONParser();
        JSONObject airflowResponse = (JSONObject) jsonParser.parse(
                new InputStreamReader(response.getContent(), UTF_8));

        System.out.println(airflowResponse);
        log.debug("Airflow response - " + airflowResponse);

        return airflowResponse;
    }
}