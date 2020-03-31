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
import org.awaitility.Awaitility;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.osdu.common.FilesKeeper.*;
import static com.osdu.core.data.parser.JsonParser.readJson;
import static com.osdu.core.data.provider.TestData.*;
import static com.osdu.core.data.provider.TestData.STATUS;
import static io.restassured.RestAssured.given;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.apache.http.HttpStatus.*;
import static org.awaitility.Awaitility.await;

public class WorkflowAnyCloudTests extends BaseWorkflowService {
    FactoriesHolder factoriesHolder = new FactoriesHolder();

    /**
     * Services paths
     */
    String startWorkflow = factoriesHolder.remoteFactoryCreator().getWorkflowService("startWorkflow");
    String getWorkflowStatus = factoriesHolder.remoteFactoryCreator().getWorkflowService("getStatus");

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Valid flow send request with all required fields and with auth token")
    public void i1_checkWorkflowStatusWithHeaders(Map<String, String> data) {
        String bodyRequestWithTheUniqueId = String.format((readJson(requestForStartWorkflowTemplate).toString()), data.get(WORKFLOW_TYPE_INGEST), data.get(DATA_TYPE_LOG));
        TestReporter.reportStep("Insert unique id into request %s", bodyRequestWithTheUniqueId);
        //TODO :: context should not be empty
        Response workflowResponse = given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(bodyRequestWithTheUniqueId)
                .when()
                .post(startWorkflow);

        workflowResponse
                .then()
                .statusCode(SC_OK)
                .and()
                .assertThat().body(WORKFLOW_ID, Matchers.notNullValue())
                .log()
                .all();

        String workflowId = workflowResponse.then()
                .extract()
                .path(WORKFLOW_ID);

        String requestForIngestStatus = String.format(readJson(requestForWorkflowStatusTemplate).toString(), workflowId);

        Awaitility.setDefaultPollDelay(15, TimeUnit.SECONDS);
        await()
                .atMost(1, MINUTES)
                .with()
                .pollInterval(10, TimeUnit.SECONDS)
                .until(() -> given()
                                .filter(new AllureRestAssured())
                                .spec(baseRequestSpec(specifiedHeadersSet()))
                                .body(requestForIngestStatus)
                                .log()
                                .method()
                                .when()
                                .post(getWorkflowStatus).jsonPath().get(STATUS),         //TODO :: status should be finished
                        s -> s.equals(data.get(STATUS)));

        TestReporter.reportStep("Job status is completed");
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Send request with all required fields and without auth tokens")
    public void i2_checkStartWorkflowWithoutHeaders(Map<String, String> data) {
        String bodyRequestWithTheUniqueId = String.format((readJson(requestForStartWorkflowTemplate).toString()), data.get(WORKFLOW_TYPE_INGEST), data.get(DATA_TYPE_LOG));
        TestReporter.reportStep("Insert unique id into request %s", bodyRequestWithTheUniqueId);
        //TODO :: context should not be empty
        Response workflowResponse = given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(new HashMap<>()))
                .body(bodyRequestWithTheUniqueId)
                .when()
                .post(startWorkflow);

        workflowResponse
                .then()
                .statusCode(SC_UNAUTHORIZED)
                .and()
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Send request without one of the required field and without auth tokens")
    public void i3_checkStartWorkflowWithoutContext(Map<String, String> data) {
        String bodyRequestWithTheUniqueId = String.format((readJson(requestForStartWorkflowWithoutContext).toString()), data.get(WORKFLOW_TYPE_INGEST), data.get(DATA_TYPE_LOG));
        TestReporter.reportStep("Insert unique id into request %s", bodyRequestWithTheUniqueId);
        //TODO :: context should not be empty
        Response workflowResponse = given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(bodyRequestWithTheUniqueId)
                .when()
                .post(startWorkflow);

        workflowResponse
                .then()
                .statusCode(SC_BAD_REQUEST)
                .assertThat().body(MESSAGE, Matchers.containsString(data.get(ERROR_CONSTRAINT_VIOLATION)))
                .and()
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Send request with empty body and with auth token")
    public void i4_checkStartWorkflowWWithEmptyBody(Map<String, String> data) {
        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(StringUtils.EMPTY)
                .when()
                .post(startWorkflow)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .and()
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Send request without body and without auth tokens")
    public void i5_checkStartWorkflowWithEmptyFields(Map<String, String> data) {
        String bodyRequestWithTheUniqueId = String.format((readJson(requestForStartWorkflowWithoutContext).toString()), StringUtils.EMPTY, StringUtils.EMPTY);
        TestReporter.reportStep("Insert unique id into request %s", bodyRequestWithTheUniqueId);
        //TODO :: context should not be empty
        Response workflowResponse = given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(bodyRequestWithTheUniqueId)
                .when()
                .post(startWorkflow);

        workflowResponse
                .then()
                .statusCode(SC_BAD_REQUEST)
                .assertThat().body(MESSAGE, Matchers.containsString(data.get(ERROR_INVALID_FORMAT)))
                .and()
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Send request with invalid data type field and without auth tokens")
    public void i6_checkStartWorkflowWithInvalidDataType(Map<String, String> data) {
        String bodyRequestWithTheUniqueId = String.format((readJson(requestForStartWorkflowWithoutContext).toString()), data.get(WORKFLOW_TYPE_INGEST), data.get(DATA_TYPE_INVALID));
        TestReporter.reportStep("Insert unique id into request %s", bodyRequestWithTheUniqueId);
        //TODO :: context should not be empty
        Response workflowResponse = given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(bodyRequestWithTheUniqueId)
                .when()
                .post(startWorkflow);

        workflowResponse
                .then()
                .statusCode(SC_BAD_REQUEST)
                .assertThat().body(MESSAGE, Matchers.containsString(data.get(ERROR_INVALID_FORMAT)))
                .and()
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Send request with invalid workflow type field and without auth tokens")
    public void i7_checkStartWorkflowWithInvalidWorkflowType(Map<String, String> data) {
        String bodyRequestWithTheUniqueId = String.format((readJson(requestForStartWorkflowWithoutContext).toString()), data.get(DATA_TYPE_INVALID), data.get(DATA_TYPE_LOG));
        TestReporter.reportStep("Insert unique id into request %s", bodyRequestWithTheUniqueId);
        //TODO :: context should not be empty
        Response workflowResponse = given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(bodyRequestWithTheUniqueId)
                .when()
                .post(startWorkflow);

        workflowResponse
                .then()
                .statusCode(SC_BAD_REQUEST)
                .assertThat().body(MESSAGE, Matchers.containsString(data.get(ERROR_INVALID_FORMAT)))
                .and()
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Send request with mismatched values and without auth tokens")
    public void i8_checkStartWorkflowWithMismatchedValues(Map<String, String> data) {
        String bodyRequestWithTheUniqueId = String.format((readJson(requestForStartWorkflowWithMismatchedvalues).toString()), data.get(DATA_TYPE_INVALID), data.get(DATA_TYPE_LOG));
        TestReporter.reportStep("Insert unique id into request %s", bodyRequestWithTheUniqueId);
        //TODO :: context should not be empty
        Response workflowResponse = given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(bodyRequestWithTheUniqueId)
                .when()
                .post(startWorkflow);

        workflowResponse
                .then()
                .statusCode(SC_BAD_REQUEST)
                //   .assertThat().body(MESSAGE, Matchers.containsString(data.get(ERROR_INVALID_FORMAT)))
                .and()
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Valid flow send request with all required fields and with auth token")
    public void i9_checkWorkflowStatusWithHeaders(Map<String, String> data) {
        String bodyRequestWithTheUniqueId = String.format((readJson(requestForStartWorkflowTemplate).toString()), data.get(WORKFLOW_TYPE_OSDU), data.get(DATA_TYPE_LOG));
        TestReporter.reportStep("Insert unique id into request %s", bodyRequestWithTheUniqueId);
        //TODO :: context should not be empty
        Response workflowResponse = given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(bodyRequestWithTheUniqueId)
                .when()
                .post(startWorkflow);

        workflowResponse
                .then()
                .statusCode(SC_BAD_REQUEST)
                .and()
                .log()
                .all();
    }
}