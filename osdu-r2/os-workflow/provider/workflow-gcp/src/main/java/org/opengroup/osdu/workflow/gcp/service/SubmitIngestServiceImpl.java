/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.workflow.gcp.service;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.opengroup.osdu.workflow.exception.OsduRuntimeException;
import org.opengroup.osdu.workflow.gcp.property.AirflowProperties;
import org.opengroup.osdu.workflow.service.SubmitIngestService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmitIngestServiceImpl implements SubmitIngestService {

  final AirflowProperties airflowProperties;
  final GoogleIapHelper googleIapHelper;

  @Override
  public boolean submitIngest(String dagName, Map<String, Object> data) {

    try {
      String airflowUrl = airflowProperties.getUrl();
      String iapClientId = googleIapHelper.getIapClientId(airflowUrl);
      String webServerUrl = format("%s/api/experimental/dags/%s/dag_runs", airflowUrl, dagName);
      HttpRequest request = googleIapHelper.buildIapRequest(webServerUrl, iapClientId, data);
      HttpResponse response = request.execute();

      String airflowResponse = IOUtils.toString(response.getContent(), UTF_8);
      log.debug("Airflow response - " + airflowResponse);

      return true;
    } catch (IOException e) {
      throw new OsduRuntimeException("Request execution exception", e);
    }
  }
}
