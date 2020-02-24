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

import com.osdu.core.airflow.SubmitIngestServiceImpl;
import com.osdu.core.data.provider.DataProviders;
import com.osdu.core.endpoints.factories.FactoriesHolder;
import com.osdu.core.reporter.TestReporter;
import io.qameta.allure.Description;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.awaitility.Awaitility;
import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.testng.Assert;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
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

public class IngestWithManifestTests extends BaseIngestService { //todo::::     add creds to env variable!!!!!!!
    FactoriesHolder factoriesHolder = new FactoriesHolder();

    /**
     * Services paths
     */
    String submitFunction = factoriesHolder.remoteFactoryCreator().getIngest("submitWithManifestFunction");

    String getWorkflowStatus = factoriesHolder.remoteFactoryCreator().getWorkflowService("getStatusFunction");
    String startWorkflow = factoriesHolder.remoteFactoryCreator().getWorkflowService("startWorkflowFunction");

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Verify required dag was called")
    public void checkDagForOsduType(Map<String, String> data) {
        DateTime dateTimeBeforeFileCreation = DateTime.now(DateTimeZone.UTC);
        TestReporter.reportStep("Create time range and save time before file creation: %s", dateTimeBeforeFileCreation);

        String workflowRequest = String.format((readJson(requestForStartWorkflowTemplate).toString()), data.get(WORKFLOW_TYPE_OSDU), data.get(DATA_TYPE_OSDU));
        TestReporter.reportStep("Created via template request to the workflow %s", workflowRequest);

        Response ingestResponse = given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(workflowRequest)
                .when()
                .post(startWorkflow);

        ingestResponse
                .then()
                .statusCode(SC_OK)
                .and()
                .assertThat().body(WORKFLOW_ID, Matchers.notNullValue())
                .log()
                .all();

        TestReporter.reportStep("Get list of the dags and save time for the %s dag", data.get(AIRFLOW_DAG_ID_OSDU));
        List<HashMap<String, String>> dagList = (List<HashMap<String, String>>) SubmitIngestServiceImpl.submitIngest().get(AIRFLOW_ITEMS);

        String executionTimeWithoutSecond = dagList
                .stream()
                .filter(x -> x.get(AIRFLOW_DAG_ID).equals(data.get(AIRFLOW_DAG_ID_OSDU)))
                .findFirst()
                .get()
                .get(AIRFLOW_EXECUTION_TIME);

        String executionSecondsBeforeParsing = dagList
                .stream()
                .filter(x -> x.get(AIRFLOW_DAG_ID).equals(data.get(AIRFLOW_DAG_ID_OSDU)))
                .findFirst()
                .get()
                .get(AIRFLOW_DAG_RUN_URL);

        String executionSeconds = StringUtils.substringBetween(executionSecondsBeforeParsing, "%3A", "&").substring(5);
        String executionTime = executionTimeWithoutSecond + ":" + executionSeconds;

        TestReporter.reportStep(executionTime);
        String timePattern = "yyyy-MM-dd HH:mm:ss";

        DateTime dateTimeAfterFileCreation = DateTime.now(DateTimeZone.UTC);
        TestReporter.reportStep("Create time range and save time after file creation", dateTimeAfterFileCreation);

        DateTimeFormatter formatter = DateTimeFormat.forPattern(timePattern);
        DateTime parsedReceivedString = formatter
                .withZone(DateTimeZone.UTC)
                .parseDateTime(executionTime);

        TestReporter.reportStep("Time before file creation: %s", dateTimeBeforeFileCreation);
        TestReporter.reportStep("Exactly time for created file and parsed: %s", parsedReceivedString);
        TestReporter.reportStep("Time after file creation: %s", dateTimeAfterFileCreation);

        TestReporter.reportStep("Verify received time in the following range");
        Assert.assertTrue(parsedReceivedString.isAfter(dateTimeBeforeFileCreation) && parsedReceivedString.isBefore(dateTimeAfterFileCreation));
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Valid flow send request with all required fields for well log data type and with auth token")
    public void i1_checkIngestWithManifest(Map<String, String> data) {
        String bodyRequest = readJson(requestForIngestWithManifest).toString();

        Response ingestResponse = given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(bodyRequest)
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
    public void i2_checkIngestSubmitWithoutHeaders(Map<String, String> data) {
        String bodyRequest = readJson(requestForIngestWithManifest).toString();

        Response ingestFunction = given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(new HashMap<>()))
                .body(bodyRequest)
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
    public void i3_checkIngestSubmitWithoutWithoutOnOfTheRequiredFields(Map<String, String> data) {
        Response ingestFunction = given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(readJson(requestForIngestWithInvalidManifest))
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
    @Description("Send request without wpc and without auth token")
    public void i4_checkIngestSubmitWithoutOnOfTheRequiredFieldsAndTokens(Map<String, String> data) {
        Response ingestFunction = given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(new HashMap<>()))
                .body(readJson(requestForIngestWithInvalidManifest))
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

    @Ignore //todo: there is no validation for the body
    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Send request with the empty values with auth token")
    public void i6_checkIngestSubmitWithEmptyValues(Map<String, String> data) {
    }

    @Ignore //todo: there is no validation for the body
    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Send request with mismatched types and with auth token")
    public void i7_checkIngestWithMismatchedTypes(Map<String, String> data) {
    }

    @Ignore //todo: there is no validation for the body
    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Send request without srn and with auth token")
    public void i8_checkIngestWithoutSrn(Map<String, String> data) {
    }
}