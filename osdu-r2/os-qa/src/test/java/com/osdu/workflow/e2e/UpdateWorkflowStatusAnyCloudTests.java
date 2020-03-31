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
import static io.restassured.RestAssured.given;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.apache.http.HttpStatus.*;
import static org.awaitility.Awaitility.await;

public class UpdateWorkflowStatusAnyCloudTests extends BaseWorkflowService {
    FactoriesHolder factoriesHolder = new FactoriesHolder();

    /**
     * Services paths
     */
    String startWorkflow = factoriesHolder.remoteFactoryCreator().getWorkflowService("startWorkflow");
    String getWorkflowStatus = factoriesHolder.remoteFactoryCreator().getWorkflowService("getStatus");
    String updateStatusWorkflowStatus = factoriesHolder.remoteFactoryCreator().getWorkflowService("updateStatus");

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Valid flow check running status with all required fields and with auth token")
    public void i1_checkWorkflowUpdateStatus(Map<String, String> data) {
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
                                .post(getWorkflowStatus).jsonPath().get(STATUS),
                        s -> s.equals(data.get(STATUS)));

        TestReporter.reportStep("Job status is submitted");

        String requestForUpdateWfStatus = String.format(readJson(requestForWorkflowUpdateStatusTemplate).toString(), data.get(STATUS_RUNNING), workflowId);

        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(requestForUpdateWfStatus)
                .when()
                .post(updateStatusWorkflowStatus)
                .then()
                .statusCode(SC_OK);

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
                                .post(getWorkflowStatus).jsonPath().get(STATUS),
                        s -> s.equals(data.get(STATUS_RUNNING)));

        TestReporter.reportStep("Job status is running");
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Valid flow check finished status with all required fields and with auth token")
    public void i2_checkWorkflowUpdateStatus(Map<String, String> data) {
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
                                .post(getWorkflowStatus).jsonPath().get(STATUS),
                        s -> s.equals(data.get(STATUS)));

        TestReporter.reportStep("Job status is submitted");

        String requestForUpdateWfStatus = String.format(readJson(requestForWorkflowUpdateStatusTemplate).toString(), data.get(STATUS_FINISHED), workflowId);

        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(requestForUpdateWfStatus)
                .when()
                .post(updateStatusWorkflowStatus)
                .then()
                .statusCode(SC_OK)
                .log()
                .all();

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
                                .post(getWorkflowStatus).jsonPath().get(STATUS),
                        s -> s.equals(data.get(STATUS_FINISHED)));

        TestReporter.reportStep("Job status is finished");
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Valid flow check failed status with all required fields and with auth token")
    public void i3_checkWorkflowUpdateStatus(Map<String, String> data) {
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
                                .post(getWorkflowStatus).jsonPath().get(STATUS),
                        s -> s.equals(data.get(STATUS)));

        TestReporter.reportStep("Job status is submitted");

        String requestForUpdateWfStatus = String.format(readJson(requestForWorkflowUpdateStatusTemplate).toString(), data.get(STATUS_FAILED), workflowId);

        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(requestForUpdateWfStatus)
                .when()
                .post(updateStatusWorkflowStatus)
                .then()
                .statusCode(SC_OK)
                .log()
                .all();

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
                                .post(getWorkflowStatus).jsonPath().get(STATUS),
                        s -> s.equals(data.get(STATUS_FAILED)));

        TestReporter.reportStep("Job status is failed");
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Valid flow check running status with all required fields and without auth token")
    public void i4_checkWorkflowUpdateStatus(Map<String, String> data) {
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
                                .post(getWorkflowStatus).jsonPath().get(STATUS),
                        s -> s.equals(data.get(STATUS)));

        TestReporter.reportStep("Job status is submitted");

        String requestForUpdateWfStatus = String.format(readJson(requestForWorkflowUpdateStatusTemplate).toString(), data.get(STATUS_RUNNING), workflowId);

        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(new HashMap<>()))
                .body(requestForUpdateWfStatus)
                .when()
                .post(updateStatusWorkflowStatus)
                .then()
                .statusCode(SC_UNAUTHORIZED);
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Update submitted status to the same status and with auth token")
    public void i5_checkWorkflowUpdateStatus(Map<String, String> data) {
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
                                .post(getWorkflowStatus).jsonPath().get(STATUS),
                        s -> s.equals(data.get(STATUS)));

        TestReporter.reportStep("Job status is submitted");

        String requestForUpdateWfStatus = String.format(readJson(requestForWorkflowUpdateStatusTemplate).toString(), data.get(STATUS), workflowId);

        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(requestForUpdateWfStatus)
                .when()
                .post(updateStatusWorkflowStatus)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .log()
                .all();
    }


    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Send request with all required fields and with the not existed wf id and without auth tokens")
    public void i6_checkUpdateWorkflowStatusWithNotExistedId(Map<String, String> data) {
        String uniqueID = UUID.randomUUID().toString();
        TestReporter.reportStep("Create unique id %s", uniqueID);

        String requestForUpdateWfStatus = String.format(readJson(requestForWorkflowUpdateStatusTemplate).toString(), data.get(STATUS), uniqueID);

        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(requestForUpdateWfStatus)
                .when()
                .post(updateStatusWorkflowStatus)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .log()
                .all();
    }


    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Send request with empty values and without auth tokens")
    public void i7_checkUpdateWorkflowStatusWithNotExistedId(Map<String, String> data) {
        String requestForUpdateWfStatus = String.format(readJson(requestForWorkflowUpdateStatusTemplate).toString(), StringUtils.EMPTY, StringUtils.EMPTY);

        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(requestForUpdateWfStatus)
                .when()
                .post(updateStatusWorkflowStatus)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Send request with empty body and with auth token")
    public void i8_checkUpdateWorkflowStatusWithEmptyBody(Map<String, String> data) {
        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(StringUtils.EMPTY)
                .when()
                .post(updateStatusWorkflowStatus)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .and()
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Send request without id and with auth tokens")
    public void i9_checkUpdateWorkflowStatusWithoutId(Map<String, String> data) {
        String requestForUpdateWfStatus = String.format(readJson(requestForWorkflowUpdateStatusWithoutId).toString(), data.get(STATUS_RUNNING));

        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(requestForUpdateWfStatus)
                .when()
                .post(updateStatusWorkflowStatus)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Send request with long id and with auth tokens")
    public void i10_checkUpdateWorkflowStatusWithLongId(Map<String, String> data) {
        String requestForUpdateWfStatus = String.format(readJson(requestForWorkflowUpdateStatusWithLongId).toString(), data.get(STATUS_RUNNING));

        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(requestForUpdateWfStatus)
                .when()
                .post(updateStatusWorkflowStatus)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Send request with mismatched values and with auth tokens")
    public void i11_checkUpdateWorkflowStatusWithMismatchedValues(Map<String, String> data) {
        String requestForUpdateWfStatus = String.format(readJson(requestForWorkflowUpdateStatusWithMismatchedValue).toString(), data.get(STATUS_RUNNING));

        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(requestForUpdateWfStatus)
                .when()
                .post(updateStatusWorkflowStatus)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .and()
                .assertThat().body(MESSAGE, Matchers.containsString(data.get(ERROR_TYPE_MISMATCH)))
                .log()
                .all();
    }
}