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

package com.osdu.ingest.e2e;

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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.osdu.common.FilesKeeper.*;
import static com.osdu.core.data.parser.JsonParser.readJson;
import static com.osdu.core.data.provider.TestData.*;
import static io.restassured.RestAssured.given;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.apache.http.HttpStatus.*;
import static org.awaitility.Awaitility.await;

public class IngestAnyCloudTests extends BaseIngestService { //todo::::     add creds to env variable!!!!!!!
    FactoriesHolder factoriesHolder = new FactoriesHolder();

    /**
     * Services paths
     */
    String submitFunction = factoriesHolder.remoteFactoryCreator().getIngest("submit");
    String getLocation = factoriesHolder.remoteFactoryCreator().getFileService("getLocation");

    String getWorkflowStatus = factoriesHolder.remoteFactoryCreator().getWorkflowService("getStatus");

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Valid flow send request with all required fields for well log data type and with auth token")
    public void i1_checkIngestByFile(Map<String, String> data) {

        String uniqueID = UUID.randomUUID().toString();
        TestReporter.reportStep("Create unique id %s", uniqueID);

        String bodyRequestWithTheUniqueId = String.format((readJson(requestFileServicePath).toString()), uniqueID);
        TestReporter.reportStep("Insert unique id into request %s", bodyRequestWithTheUniqueId);

        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(bodyRequestWithTheUniqueId)
                .when()
                .post(getLocation)
                .then()
                .statusCode(SC_OK)
                .and()
                .assertThat().body(FILE_ID, Matchers.is(uniqueID))
                .assertThat().body(SIGNED_URL, Matchers.notNullValue())
                .log()
                .all();

        String requestWithTheNewCreatedId = String.format((readJson(requestForIngestTemplate).toString()), data.get(DATA_TYPE_LOG), uniqueID);
        TestReporter.reportStep("Created via template request to the ingest %s", requestWithTheNewCreatedId);

        Response ingestResponse = given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(requestWithTheNewCreatedId)
                .when()
                .post(submitFunction);

        ingestResponse
                .then()
                .statusCode(SC_OK)
                .and()
                .assertThat().body(WORKFLOW_ID, Matchers.notNullValue())
                .log()
                .all();

        String workflowId = ingestResponse.then()
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
    @Description("Valid flow send request with all required fields for opaque data type and with auth token")
    public void i2_checkIngestByFile(Map<String, String> data) {

        String uniqueID = UUID.randomUUID().toString();
        TestReporter.reportStep("Create unique id %s", uniqueID);

        String bodyRequestWithTheUniqueId = String.format((readJson(requestFileServicePath).toString()), uniqueID);
        TestReporter.reportStep("Insert unique id into request %s", bodyRequestWithTheUniqueId);

        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(bodyRequestWithTheUniqueId)
                .when()
                .post(getLocation)
                .then()
                .statusCode(SC_OK)
                .and()
                .assertThat().body(FILE_ID, Matchers.is(uniqueID))
                .assertThat().body(SIGNED_URL, Matchers.notNullValue())
                .log()
                .all();

        String requestWithTheNewCreatedId = String.format((readJson(requestForIngestTemplate).toString()), data.get(DATA_TYPE_OPAQUE), uniqueID);
        TestReporter.reportStep("Created via template request to the ingest %s", requestWithTheNewCreatedId);

        Response ingestResponse = given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(requestWithTheNewCreatedId)
                .when()
                .post(submitFunction);

        ingestResponse
                .then()
                .statusCode(SC_OK)
                .and()
                .assertThat().body(WORKFLOW_ID, Matchers.notNullValue())
                .log()
                .all();

        String workflowId = ingestResponse.then()
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
    public void i3_checkIngestSubmitWithoutHeaders(Map<String, String> data) {
        String uniqueID = UUID.randomUUID().toString();
        TestReporter.reportStep("Create unique id %s", uniqueID);

        String bodyRequestWithTheUniqueId = String.format((readJson(requestFileServicePath).toString()), uniqueID);
        TestReporter.reportStep("Insert unique id into request %s", bodyRequestWithTheUniqueId);

        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(bodyRequestWithTheUniqueId)
                .when()
                .post(getLocation)
                .then()
                .statusCode(SC_OK)
                .and()
                .assertThat().body(FILE_ID, Matchers.is(uniqueID))
                .assertThat().body(SIGNED_URL, Matchers.notNullValue())
                .log()
                .all();

        String requestWithTheNewCreatedId = String.format((readJson(requestFileServicePath).toString()), uniqueID, data.get(DATA_TYPE_LOG));

        Response ingestFunction = given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(new HashMap<>()))
                .body(requestWithTheNewCreatedId)
                .when()
                .post(submitFunction);

        ingestFunction
                .then()
                .statusCode(SC_UNAUTHORIZED)
                .and()
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Send request without FileID field and with auth token")
    public void i4_checkIngestSubmitWithoutWithoutOnOfTheRequiredFields(Map<String, String> data) {
        Response ingestFunction = given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(readJson(requestForIngestWithoutFileId))
                .when()
                .post(submitFunction);

        ingestFunction
                .then()
                .statusCode(SC_BAD_REQUEST)
                .and()
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Send request with empty body and with auth token")
    public void i5_checkIngestSubmitWithEmptyBody(Map<String, String> data) {
        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(StringUtils.EMPTY)
                .when()
                .post(submitFunction)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .and()
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Send empty request with auth token")
    public void i6_checkIngestSubmitWithEmptyValues(Map<String, String> data) {
        String requestWithTheNewCreatedId = String.format((readJson(requestForIngestTemplate).toString()), StringUtils.EMPTY, StringUtils.EMPTY);
        TestReporter.reportStep("Created via template request to the ingest %s", requestWithTheNewCreatedId);

        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(requestWithTheNewCreatedId)
                .when()
                .post(submitFunction)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .and()
                .assertThat().body(MESSAGE, Matchers.containsString(data.get(ERROR_INVALID_FORMAT)))
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Send request with invalid dataType and with auth token")
    public void i7_checkIngestWithInvalidDataType(Map<String, String> data) {
        String uniqueID = UUID.randomUUID().toString();
        TestReporter.reportStep("Create unique id %s", uniqueID);

        String bodyRequestWithTheUniqueId = String.format((readJson(requestFileServicePath).toString()), uniqueID);
        TestReporter.reportStep("Insert unique id into request %s", bodyRequestWithTheUniqueId);

        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(bodyRequestWithTheUniqueId)
                .when()
                .post(getLocation)
                .then()
                .statusCode(SC_OK)
                .and()
                .assertThat().body(FILE_ID, Matchers.is(uniqueID))
                .assertThat().body(SIGNED_URL, Matchers.notNullValue())
                .log()
                .all();

        String requestWithTheNewCreatedId = String.format((readJson(requestForIngestTemplate).toString()), data.get(DATA_TYPE_INVALID), uniqueID);
        TestReporter.reportStep("Created via template request to the ingest %s", requestWithTheNewCreatedId);

        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(requestWithTheNewCreatedId)
                .when()
                .post(submitFunction)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .and()
                .assertThat().body(MESSAGE, Matchers.containsString(data.get(ERROR_INVALID_FORMAT)))
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Send request with not existed file id and with auth token")
    public void i8_checkIngestWithNotExistedFileId(Map<String, String> data) {
        String uniqueID = UUID.randomUUID().toString();
        TestReporter.reportStep("Create unique id %s", uniqueID);
        String bodyRequestWithTheUniqueId = String.format((readJson(requestFileServicePath).toString()), uniqueID);
        TestReporter.reportStep("Insert unique id into request %s", bodyRequestWithTheUniqueId);

        String requestWithTheNewCreatedId = String.format((readJson(requestForIngestTemplate).toString()), data.get(DATA_TYPE_LOG), uniqueID);
        TestReporter.reportStep("Created via template request to the ingest %s", requestWithTheNewCreatedId);

        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(requestWithTheNewCreatedId)
                .when()
                .post(submitFunction)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .and()
                // .assertThat().body(MESSAGE, Matchers.containsString(data.get(ERROR_INVALID_FORMAT)))
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Send request with mismatched dataType value and with auth token")
    public void i9_checkIngestWithMismatchedValueForDataType(Map<String, String> data) {
        String uniqueID = UUID.randomUUID().toString();
        TestReporter.reportStep("Create unique id %s", uniqueID);

        String bodyRequestWithTheUniqueId = String.format((readJson(requestFileServicePath).toString()), uniqueID);
        TestReporter.reportStep("Insert unique id into request %s", bodyRequestWithTheUniqueId);

        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(bodyRequestWithTheUniqueId)
                .when()
                .post(getLocation)
                .then()
                .statusCode(SC_OK)
                .and()
                .assertThat().body(FILE_ID, Matchers.is(uniqueID))
                .assertThat().body(SIGNED_URL, Matchers.notNullValue())
                .log()
                .all();

        String requestWithTheNewCreatedId = String.format((readJson(requestForIngestWithmismathedDataTypeValue).toString()), uniqueID);
        TestReporter.reportStep("Created via template request to the ingest %s", requestWithTheNewCreatedId);

        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(requestWithTheNewCreatedId)
                .when()
                .post(submitFunction)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .and()
                .assertThat().body(MESSAGE, Matchers.containsString(data.get(ERROR_INVALID_FORMAT)))
                .log()
                .all();
    }
}