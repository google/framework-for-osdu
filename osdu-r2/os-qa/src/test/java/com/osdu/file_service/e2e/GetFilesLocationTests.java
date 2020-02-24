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

package com.osdu.file_service.e2e;

import com.osdu.core.data.provider.DataProviders;
import com.osdu.core.endpoints.factories.FactoriesHolder;
import com.osdu.core.reporter.TestReporter;
import io.qameta.allure.Description;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.response.Response;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

import static com.osdu.common.FilesKeeper.*;
import static com.osdu.core.data.parser.JsonParser.readJson;
import static com.osdu.core.data.provider.TestData.*;
import static com.osdu.core.data.provider.TestData.LOCATION;
import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.*;

public class GetFilesLocationTests extends BaseFileService {
    FactoriesHolder factoriesHolder = new FactoriesHolder();
    String timePattern = "yyyy-MM-dd'T'HH:mm:ss.SSS+'0000'";

    /**
     * File service paths
     */
    String getLocation = factoriesHolder.remoteFactoryCreator().getFileService("getLocationFunction");
    String getFileLocation = factoriesHolder.remoteFactoryCreator().getFileService("getFileLocationFunction");
    String getFilesListFunction = factoriesHolder.remoteFactoryCreator().getFileService("getFileListFunction");

    @SneakyThrows
    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Valid flow send request with all required fields and with auth token")
    public void i1_checkNewCreatedFileIsReturnedByFilesList(Map<String, String> data) {

        DateTime dateTimeBeforeFileCreation = DateTime.now(DateTimeZone.UTC);
        TestReporter.reportStep("Create time range and save time before file creation: %s", dateTimeBeforeFileCreation);

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

        Response fileLocation = given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(bodyRequestWithTheUniqueId)
                .when()
                .post(getFileLocation);

        fileLocation
                .then()
                .statusCode(SC_OK)
                .and()
                .assertThat().body(DRIVER, Matchers.is(data.get(DRIVER)))
                .assertThat().body(LOCATION, Matchers.notNullValue())
                .log()
                .all();

        String receivedUrl = fileLocation
                .then()
                .extract()
                .path(LOCATION);

        //Delay in order to avoid test failing because of the fast speed of the tests running
        Thread.sleep(4000);
        DateTime dateTimeAfterFileCreation = DateTime.now(DateTimeZone.UTC);
        TestReporter.reportStep("Create time range and save time after file creation", dateTimeAfterFileCreation);
        String requestForFiles = String.format((readJson(requestTemplateForFiles).toString()), dateTimeBeforeFileCreation.toString(), data.get(USER_ID), dateTimeAfterFileCreation.toString());
        System.out.println(requestForFiles);

        Response filesLocation = given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(requestForFiles)
                .when()
                .post(getFilesListFunction);

        filesLocation
                .then()
                .statusCode(SC_OK)
                .and()
                //       .assertThat().body(GET_LOCATION_FROM_FILES, Matchers.contains(receivedUrl))
                //      .assertThat().body(GET_FILE_ID_FROM_FILES, Matchers.contains(uniqueID))
                //      .assertThat().body(GET_CREATOR_FROM_FILES, Matchers.contains(data.get(USER_ID)))
                .log()
                .all();

        List<HashMap<String, String>> jsonArray = filesLocation
                .then()
                .extract()
                .path(CONTENT);

        System.out.println(jsonArray);
        HashMap<String, String> contentValues = jsonArray.get(0);

        Assert.assertTrue(contentValues.values().contains(data.get(USER_ID)));
        Assert.assertTrue(contentValues.values().contains(uniqueID));
        Assert.assertTrue(contentValues.values().contains(receivedUrl));

        String receivedTimeSavedIntoString = contentValues.get(GET_CREATION_TIME_FROM_FILES);

        DateTimeFormatter formatter = DateTimeFormat.forPattern(timePattern);
        DateTime parsedReceivedString = formatter
                .withZone(DateTimeZone.UTC)
                .parseDateTime(receivedTimeSavedIntoString);

        TestReporter.reportStep("Time before file creation: %s", dateTimeBeforeFileCreation);
        TestReporter.reportStep("Exactly time for created file and parsed: %s", parsedReceivedString);
        TestReporter.reportStep("Time after file creation: %s", dateTimeAfterFileCreation);

        TestReporter.reportStep("Verify received time in the following rage");
        Assert.assertTrue(parsedReceivedString.isAfter(dateTimeBeforeFileCreation) && parsedReceivedString.isBefore(dateTimeAfterFileCreation));
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Send request with all required fields and without auth tokens")
    public void i2_checkFilesListAccessWithoutHeaders(Map<String, String> data) {
        Response filesLocation = given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(new HashMap<>()))
                .body(readJson(requestForFilesWithTime).toString())
                .when()
                .post(getFilesListFunction);

        filesLocation
                .then()
                .statusCode(SC_UNAUTHORIZED)
                .and()
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Send request without timeTo field and with auth token")
    public void i3_checkFilesListAccessWithoutOnOfTheRequiredFields(Map<String, String> data) {
        Response filesLocation = given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(readJson(requestForFilesWithoutTime).toString())
                .when()
                .post(getFilesListFunction);

        filesLocation
                .then()
                .statusCode(SC_BAD_REQUEST)
                .and()
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Valid flow send request with all required fields and with auth token")
    public void i4_checkFilesListAccessWithoutOnOfTheRequiredFields(Map<String, String> data) {
        Response filesLocation = given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(readJson(requestForFilesWithoutItems).toString())
                .when()
                .post(getFilesListFunction);

        filesLocation
                .then()
                .statusCode(SC_BAD_REQUEST)
                .and()
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Send empty request with auth token")
    public void i5_checkFilesListAccessWithEmptyRequest(Map<String, String> data) {
        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(StringUtils.EMPTY)
                .when()
                .post(getFilesListFunction)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .and()
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Send request with string for PageNum and with auth token")
    public void i6_checkFilesListAccessWithFieldsTypeMismatch(Map<String, String> data) {
        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(readJson(requestForFilesWithPageNumFieldMismatch).toString())
                .when()
                .post(getFilesListFunction)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .and()
                .assertThat().body(MESSAGE, Matchers.containsString(data.get(ERROR_TYPE_MISMATCH)))
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Send request with string for Items and with auth token")
    public void i7_checkFilesListAccessWithFieldsTypeMismatch(Map<String, String> data) {
        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(readJson(requestForFilesWithItemsFieldMismatch).toString())
                .when()
                .post(getFilesListFunction)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .and()
                .assertThat().body(MESSAGE, Matchers.containsString(data.get(ERROR_TYPE_MISMATCH)))
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Send request with string for UserId and with auth token")
    public void i8_checkFilesListAccessWithFieldsTypeMismatch(Map<String, String> data) {
        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(readJson(requestForFilesWithUserIdFieldMismatch).toString())
                .when()
                .post(getFilesListFunction)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .and()
                .assertThat().body(MESSAGE, Matchers.containsString(data.get(ERROR_TYPE_MISMATCH)))
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Send request with invalid data format and with auth token")
    public void i9_checkFilesListAccessWithInvalidData(Map<String, String> data) {
        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(readJson(requestForFilesWithInvalidData).toString())
                .when()
                .post(getFilesListFunction)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .and()
                .assertThat().body(MESSAGE, Matchers.containsString(data.get(ERROR_TIME_PARSING)))
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Send request with 'time from' before 'time to' and with auth token")
    public void i10_checkFilesListAccessWithWrongTimeRange(Map<String, String> data) {
        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(readJson(requestForFilesWithWrongTimeRange).toString())
                .when()
                .post(getFilesListFunction)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .and()
                .assertThat().body(MESSAGE, Matchers.containsString(data.get(ERROR_CONSTRAINT_VIOLATION)))
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Send request with not existed time and with auth token")
    public void i11_checkFilesListAccessWithNotExistedTime(Map<String, String> data) {
        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(readJson(requestForFilesWithNotExistedTime).toString())
                .when()
                .post(getFilesListFunction)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .and()
                .assertThat().body(MESSAGE, Matchers.containsString(data.get(ERROR_TIME_PARSING)))
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Send request with not existed time and with auth token")
    public void i12_checkFilesListAccessWithNegativeItemsNumber(Map<String, String> data) {
        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(readJson(requestForFilesWithNegativeItemsNumber).toString())
                .when()
                .post(getFilesListFunction)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .and()
                .assertThat().body(MESSAGE, Matchers.containsString(data.get(ERROR_CONSTRAINT_VIOLATION)))
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Send request with not existed time and with auth token")
    public void i13_checkFilesListAccessWithHugePageNumValue(Map<String, String> data) {
        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(readJson(requestForFilesWithHugePageNumValue).toString())
                .when()
                .post(getFilesListFunction)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .and()
                .assertThat().body(MESSAGE, Matchers.containsString(data.get(EXCEPTION)))
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Send request without data time and with auth token")
    public void i14_checkFilesListAccessWithoutDataTime(Map<String, String> data) {
        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(readJson(requestForFilesWithoutDataTime).toString())
                .when()
                .post(getFilesListFunction)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .and()
                .assertThat().body(MESSAGE, Matchers.containsString(data.get(ERROR_CONSTRAINT_VIOLATION)))
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Send request with invalid json and with auth token")
    public void i15_checkFilesListAccessWithInvalidJson(Map<String, String> data) {
        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(readJson(requestForFilesWithInvalidJson).toString().replaceAll(",", StringUtils.EMPTY))
                .when()
                .post(getFilesListFunction)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .and()
                .assertThat().body(MESSAGE, Matchers.containsString(data.get(ERROR_JSON_PARSING)))
                .log()
                .all();
    }
}