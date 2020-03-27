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

package com.osdu.file_service.e2e;

import com.osdu.core.data.provider.DataProviders;
import com.osdu.core.endpoints.factories.FactoriesHolder;
import com.osdu.core.reporter.TestReporter;
import io.qameta.allure.Description;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import static com.osdu.common.FilesKeeper.*;
import static com.osdu.core.data.parser.JsonParser.readJson;
import static com.osdu.core.data.provider.TestData.*;
import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.*;

public class GetLocationGcpTests extends BaseFileService {
    FactoriesHolder factoriesHolder = new FactoriesHolder();
    /**
     * File service paths
     */
    String getLocation = factoriesHolder.remoteFactoryCreator().getFileService("getLocation");
    String getFileLocation = factoriesHolder.remoteFactoryCreator().getFileService("getFileLocation");

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Valid flow send request with the unique id and with auth token")
    public void i1_checkNewFileCreation(Map<String, String> data) {
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

        Path path = getCreatedFile(receivedUrl);

        long receivedFileSize = 0;
        int expectedFileSize = 0;

        try {
            receivedFileSize = Files.size(path);
        } catch (IOException e) {
            TestReporter.reportErrorStep("File nod found " + e.toString());
        }
        TestReporter.reportStep("File name: " + path.getFileName() + " and size - " + receivedFileSize + " bytes.");

        Assert.assertEquals(path.getFileName().toString(), uniqueID);
        Assert.assertEquals(expectedFileSize, receivedFileSize);
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Valid flow send request with the existed id and with auth token")
    public void i2_checkFileNameAlreadyExisted(Map<String, String> data) {
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
                .log()
                .all();

        TestReporter.reportStep("Repeat request with the same id", bodyRequestWithTheUniqueId);

        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(bodyRequestWithTheUniqueId)
                .when()
                .post(getLocation)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .and()
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Valid flow send request with the unique id and without auth token")
    public void i3_checkNewFileCreationWithoutHeaders(Map<String, String> data) {
        String uniqueID = UUID.randomUUID().toString();
        TestReporter.reportStep("Create unique id %s", uniqueID);

        String bodyRequestWithTheUniqueId = String.format((readJson(requestFileServicePath).toString()), uniqueID);
        TestReporter.reportStep("Insert unique id into request %s", bodyRequestWithTheUniqueId);

        given()
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON)
                .body(bodyRequestWithTheUniqueId)
                .when()
                .post(getLocation)
                .then()
                .statusCode(SC_UNAUTHORIZED)
                .and()
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Valid flow send request to fileLocation service without auth token")
    public void i4_checkFileLocationWithoutHeaders(Map<String, String> data) {
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

        given()
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON)
                .body(bodyRequestWithTheUniqueId)
                .when()
                .post(getFileLocation)
                .then()
                .statusCode(SC_UNAUTHORIZED)
                .and()
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Valid flow send request without id and with auth token")
    public void i5_checkNewFileCreationWithoutId(Map<String, String> data) {
        Response response = given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(data.get(EMPTY_REQUEST))
                .when()
                .post(getLocation);

        response.then()
                .statusCode(SC_OK)
                .and()
                .assertThat().body(FILE_ID, Matchers.notNullValue())
                .assertThat().body(SIGNED_URL, Matchers.notNullValue())
                .log()
                .all();

        String generatedFileId = response.then()
                .extract()
                .path(FILE_ID);

        String bodyRequestWithGeneratedId = String.format((readJson(requestFileServicePath).toString()), generatedFileId);
        TestReporter.reportStep("Insert unique id into request %s", bodyRequestWithGeneratedId);

        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(bodyRequestWithGeneratedId)
                .when()
                .post(getFileLocation)
                .then()
                .statusCode(SC_OK)
                .and()
                .assertThat().body(DRIVER, Matchers.is(data.get(DRIVER)))
                .assertThat().body(LOCATION, Matchers.notNullValue())
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Valid flow send request without id and without auth token")
    public void i6_checkNewFileCreationWithoutHeaders(Map<String, String> data) {
        given()
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON)
                .body(data.get(EMPTY_REQUEST))
                .when()
                .post(getLocation)
                .then()
                .statusCode(SC_UNAUTHORIZED)
                .and()
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Send request with empty body and with auth token")
    public void i7_checkNewFileCreationWithoutBody(Map<String, String> data) {
        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(StringUtils.EMPTY)
                .when()
                .post(getLocation)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .and()
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Send request to fileLocation service with empty body and with auth token")
    public void i8_checkGetFileLocationWithoutBody(Map<String, String> data) {
        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(StringUtils.EMPTY)
                .when()
                .post(getFileLocation)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .and()
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Invalid flow send request with integer instead of string auth token")
    public void i9_checkNewFileCreationWithIntegerInRequest(Map<String, String> data) {
        String bodyRequestWithTheIntegerId = readJson(requestWithMismathchedRequestToFileServicePath).toString();
        TestReporter.reportStep("Insert integer id into request %s", bodyRequestWithTheIntegerId);

        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(bodyRequestWithTheIntegerId)
                .when()
                .post(getLocation)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .and()
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Invalid flow send request with long file id with auth token")
    public void i10_checkNewFileCreationWithLongId(Map<String, String> data) {
        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(requestWithLongIdPath)
                .when()
                .post(getLocation)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .and()
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Invalid flow send request with invalid json with auth token")
    public void i11_checkNewFileCreationWithInvalidJson(Map<String, String> data) {
        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(requestWithInvalidJsonPath)
                .when()
                .post(getLocation)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .and()
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Invalid flow send request with space in file name with auth token")
    public void i12_checkNewFileCreationWithSpaceInFileId(Map<String, String> data) {
        String bodyRequestWithTheIntegerId = String.format((readJson(requestFileServicePath).toString()), StringUtils.SPACE);
        TestReporter.reportStep("Insert space into fileId %s", bodyRequestWithTheIntegerId);

        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(bodyRequestWithTheIntegerId)
                .when()
                .post(getLocation)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .and()
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Valid flow send request without id and with auth token")
    public void i13_checkFileLocationWithoutId(Map<String, String> data) {
        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(data.get(EMPTY_REQUEST))
                .when()
                .post(getFileLocation)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .and()
                .assertThat().body(MESSAGE, Matchers.containsString(data.get(ERROR_CONSTRAINT_VIOLATION)))
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Valid flow send request without id and without auth token")
    public void i14_checkFileLocationWithoutId(Map<String, String> data) {
        given()
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON)
                .body(data.get(EMPTY_REQUEST))
                .when()
                .post(getFileLocation)
                .then()
                .statusCode(SC_UNAUTHORIZED)
                .and()
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Invalid flow send request with long file id with auth token")
    public void i15_checkFileLocationWithLongId(Map<String, String> data) {
        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(readJson(requestWithLongIdPath).toString())
                .when()
                .post(getFileLocation)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .and()
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Invalid flow send request with invalid json with auth token")
    public void i16_checkFileLocationWithInvalidJson(Map<String, String> data) {
        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(requestWithInvalidJsonPath)
                .when()
                .post(getFileLocation)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .and()
                .log()
                .all();
    }

    @Test(dataProvider = "testedData", dataProviderClass = DataProviders.class)
    @Description("Invalid flow send request with space in file name with auth token")
    public void i17_checkFileLocationWithSpaceInFileId(Map<String, String> data) {
        String bodyRequestWithTheIntegerId = String.format((readJson(requestFileServicePath).toString()), StringUtils.SPACE);
        TestReporter.reportStep("Insert space into fileId %s", bodyRequestWithTheIntegerId);

        given()
                .filter(new AllureRestAssured())
                .spec(baseRequestSpec(specifiedHeadersSet()))
                .body(bodyRequestWithTheIntegerId)
                .when()
                .post(getFileLocation)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .and()
                .log()
                .all();
    }
}