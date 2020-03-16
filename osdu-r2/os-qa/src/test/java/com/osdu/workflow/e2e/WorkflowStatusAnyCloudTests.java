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

import com.osdu.core.data.provider.DataProviders;
import com.osdu.core.endpoints.factories.FactoriesHolder;
import com.osdu.core.reporter.TestReporter;
import io.qameta.allure.Description;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.osdu.common.FilesKeeper.requestForWorkflowStatusTemplate;
import static com.osdu.core.data.parser.JsonParser.readJson;
import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.*;

public class WorkflowStatusAnyCloudTests extends BaseWorkflowService {
    FactoriesHolder factoriesHolder = new FactoriesHolder();

    /**
     * Services paths
     */
    String getWorkflowStatus = factoriesHolder.remoteFactoryCreator().getWorkflowService("getStatusFunction");

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Send request with all required fields and without auth token")
    public void i1_checkWorkflowStatusWithoutHeaders(Map<String, String> data) {
        String uniqueID = UUID.randomUUID().toString();
        TestReporter.reportStep("Create unique id %s", uniqueID);

        String bodyRequestWithTheUniqueId = String.format((readJson(requestForWorkflowStatusTemplate).toString()), uniqueID);
        TestReporter.reportStep("Insert unique id into request %s", bodyRequestWithTheUniqueId);

        Response ingestResponse = given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(new HashMap<>()))
                .body(bodyRequestWithTheUniqueId)
                .when()
                .post(getWorkflowStatus);

        ingestResponse
                .then()
                .statusCode(SC_UNAUTHORIZED)
                .and()
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Send request with empty body and with auth token")
    public void i2_checkWorkflowStatusWithEmptyBody(Map<String, String> data) {
        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(StringUtils.EMPTY)
                .when()
                .post(getWorkflowStatus)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .and()
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Send request with not existed id and with auth token")
    public void i3_checkWorkflowStatusWithNotExistedFileId(Map<String, String> data) {
        String uniqueID = UUID.randomUUID().toString();
        TestReporter.reportStep("Create unique id %s", uniqueID);

        String bodyRequestWithTheUniqueId = String.format((readJson(requestForWorkflowStatusTemplate).toString()), uniqueID);
        TestReporter.reportStep("Insert unique id into request %s", bodyRequestWithTheUniqueId);

        Response ingestResponse = given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(bodyRequestWithTheUniqueId)
                .when()
                .post(getWorkflowStatus);

        ingestResponse
                .then()
                .statusCode(SC_NOT_FOUND)
                .and()
                .log()
                .all();
    }
}