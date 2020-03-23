/*
 * Copyright 2020 Google LLC
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

package org.opengroup.osdu.delivery;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.opengroup.osdu.delivery.TestUtils.AUTHORIZATION_TOKEN;
import static org.opengroup.osdu.delivery.TestUtils.FILE_ID;
import static org.opengroup.osdu.delivery.TestUtils.PARTITION;
import static org.opengroup.osdu.delivery.TestUtils.SRG_OBJECT_URI;
import static org.opengroup.osdu.delivery.TestUtils.SRG_PROTOCOL;
import static org.opengroup.osdu.delivery.TestUtils.USER_DES_ID;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.invocation.InvocationOnMock;
import org.opengroup.osdu.core.common.model.entitlements.AuthorizationResponse;
import org.opengroup.osdu.core.common.model.file.DriverType;
import org.opengroup.osdu.core.common.model.file.FileListRequest;
import org.opengroup.osdu.core.common.model.file.FileListResponse;
import org.opengroup.osdu.core.common.model.file.FileLocation;
import org.opengroup.osdu.core.common.model.file.LocationResponse;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.provider.interfaces.IAuthorizationService;
import org.opengroup.osdu.delivery.model.SignedUrl;
import org.opengroup.osdu.delivery.provider.interfaces.FileLocationRepository;
import org.opengroup.osdu.delivery.provider.interfaces.LocationMapper;
import org.opengroup.osdu.delivery.provider.interfaces.StorageService;
import org.opengroup.osdu.delivery.util.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@DisplayNameGeneration(ReplaceCamelCase.class)
public class DeliveryFlowTest {

  private static final String SIGNED_URL_KEY = "SignedURL";
  private static final String TEMP_USER = "temp-user";

  @MockBean
  private IAuthorizationService authorizationService;
  @MockBean
  private FileLocationRepository fileLocationRepository;
  @MockBean
  private LocationMapper locationMapper;
  @MockBean
  private StorageService storageService;

  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private MockMvc mockMvc;

  @Test
  public void shouldPassGetLocationFlowWhenFileIdIsNotProvided() throws Exception {
    // given
    HttpHeaders headers = getHttpHeaders();

    given(authorizationService.authorizeAny(any(), eq("service.storage.creator")))
        .willReturn(AuthorizationResponse.builder()
            .user("user@mail.com")
            .build());

    given(storageService.createSignedUrl(anyString(), eq(AUTHORIZATION_TOKEN), eq(PARTITION)))
        .willReturn(getSignedUrl());
    given(fileLocationRepository.save(any())).willAnswer(this::getFileLocationAnswer);
    given(locationMapper.buildLocationResponse(any(SignedUrl.class), any(FileLocation.class)))
        .willAnswer(this::getLocationResponseAnswer);

    // when
    ResultActions resultActions = mockMvc.perform(
        post("/getLocation")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.displayName())
            .with(csrf())
            .headers(headers)
            .content("{}"));

    // then
    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.FileID").value(TestUtils.isValidUUID()))
        .andExpect(jsonPath("$.Location.SignedURL").value(TestUtils.isValidSingedUrl()));
  }

  @Test
  public void shouldPassGetLocationFlowWhenFileIdIsProvided() throws Exception {
    // given
    HttpHeaders headers = getHttpHeaders();

    given(authorizationService.authorizeAny(any(), eq("service.storage.creator")))
        .willReturn(AuthorizationResponse.builder()
            .user("user@mail.com")
            .build());

    given(storageService.createSignedUrl(anyString(), eq(AUTHORIZATION_TOKEN), eq(PARTITION)))
        .willReturn(getSignedUrl());
    given(fileLocationRepository.save(any())).willAnswer(this::getFileLocationAnswer);
    given(locationMapper.buildLocationResponse(any(SignedUrl.class), any(FileLocation.class)))
        .willAnswer(this::getLocationResponseAnswer);

    // when
    ResultActions resultActions = mockMvc.perform(
        post("/getLocation")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.displayName())
            .with(csrf())
            .headers(headers)
            .content("{\"FileID\": \"" + FILE_ID + "\"}"));

    // then
    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.FileID").value(FILE_ID))
        .andExpect(jsonPath("$.Location.SignedURL").value(TestUtils.isValidSingedUrl()));
  }

  @Test
  public void shouldFailGetLocationFlowUnauthorized() throws Exception {
    // given
    HttpHeaders headers = getHttpHeaders();

    given(storageService.createSignedUrl(anyString(), eq(AUTHORIZATION_TOKEN), eq(PARTITION)))
        .willReturn(getSignedUrl());
    given(fileLocationRepository.save(any())).willAnswer(this::getFileLocationAnswer);
    given(locationMapper.buildLocationResponse(any(SignedUrl.class), any(FileLocation.class)))
        .willAnswer(this::getLocationResponseAnswer);

    given(authorizationService.authorizeAny(any(), eq("service.storage.creator")))
        .willThrow(AppException.createUnauthorized("test: viewer"));

    // when
    ResultActions resultActions = mockMvc.perform(
        post("/getLocation")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.displayName())
            .with(csrf())
            .headers(headers)
            .content("{}"));

    // then
    resultActions
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("The user is not authorized to perform this action"));
  }

  @Test
  public void shouldPassGetFileLocationFlow() throws Exception {
    // given
    HttpHeaders headers = getHttpHeaders();

    given(authorizationService.authorizeAny(any(), eq("service.storage.creator")))
        .willReturn(AuthorizationResponse.builder()
            .user("user@mail.com")
            .build());

    given(fileLocationRepository.findByFileID(FILE_ID)).willReturn(getFileLocation(new Date()));

    // when
    ResultActions resultActions = mockMvc.perform(
        post("/getFileLocation")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.displayName())
            .with(csrf())
            .headers(headers)
            .content("{\"FileID\": \"" + FILE_ID + "\"}"));

    // then
    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.Driver").value(DriverType.GCS.name()))
        .andExpect(jsonPath("$.Location").value(Matchers.matchesPattern(SRG_OBJECT_URI)));
  }

  @Test
  public void shouldFailGetFileLocationFlowUnauthorized() throws Exception {
    // given
    HttpHeaders headers = getHttpHeaders();

    given(authorizationService.authorizeAny(any(), eq("service.storage.creator")))
        .willThrow(AppException.createUnauthorized("test: viewer"));

    // when
    ResultActions resultActions = mockMvc.perform(
        post("/getFileLocation")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.displayName())
            .with(csrf())
            .headers(headers)
            .content("{}"));

    // then
    resultActions
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("The user is not authorized to perform this action"));
  }

  @Test
  public void shouldPassGetFileListFlow() throws Exception {
    // given
    HttpHeaders headers = getHttpHeaders();
    LocalDateTime now = LocalDateTime.now();
    FileListRequest request = FileListRequest.builder()
        .timeFrom(now.minusHours(1))
        .timeTo(now)
        .pageNum(0)
        .items((short) 5)
        .userID(TEMP_USER)
        .build();

    given(fileLocationRepository.findAll(request)).willReturn(FileListResponse.builder()
        .content(Arrays.asList(
            getFileLocation(toDate(now.minusMinutes(10))),
            getFileLocation(toDate(now.minusMinutes(20)))))
        .number(0)
        .numberOfElements(2)
        .size(5)
        .build());

    given(authorizationService.authorizeAny(any(), eq("service.storage.creator")))
        .willReturn(AuthorizationResponse.builder()
            .user("user@mail.com")
            .build());

    // when
    ResultActions resultActions = mockMvc.perform(
        post("/getFileList")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.displayName())
            .with(csrf())
            .headers(headers)
            .content(objectMapper.writeValueAsString(request)));

    // then
    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.Content").value(Matchers.hasSize(2)))
        .andExpect(jsonPath("$.Number").value(0))
        .andExpect(jsonPath("$.NumberOfElements").value(2))
        .andExpect(jsonPath("$.Size").value(5));
  }

  @Test
  public void shouldFailGetFileListFlowUnauthorized() throws Exception {
    // given
    HttpHeaders headers = getHttpHeaders();

    given(authorizationService.authorizeAny(any(), eq("service.storage.creator")))
        .willThrow(AppException.createUnauthorized("test: viewer"));

    // when
    ResultActions resultActions = mockMvc.perform(
        post("/getFileList")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.displayName())
            .with(csrf())
            .headers(headers)
            .content("{}"));

    // then
    resultActions
        .andExpect(status().isUnauthorized());
  }

  @TestConfiguration
  @EnableWebSecurity
  @EnableGlobalMethodSecurity(prePostEnabled = true)
  public static class TestSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {

      http.httpBasic().disable()
          .csrf().disable();  //disable default authN. AuthN handled by endpoints proxy
    }

  }

  private HttpHeaders getHttpHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.add(DpsHeaders.AUTHORIZATION, AUTHORIZATION_TOKEN);
    headers.add(DpsHeaders.DATA_PARTITION_ID, PARTITION);
    return headers;
  }

  private SignedUrl getSignedUrl() {
    String bucketName = RandomStringUtils.randomAlphanumeric(4);
    String folderName = USER_DES_ID + "/" + RandomStringUtils.randomAlphanumeric(9);
    String filename = TestUtils.getUuidString();

    URI uri = TestUtils.getObjectUri(bucketName, folderName, filename);
    URL url = TestUtils.getObjectUrl(bucketName, folderName, filename);

    return SignedUrl.builder()
        .uri(uri)
        .url(url)
        .createdAt(TestUtils.now())
        .createdBy(USER_DES_ID)
        .build();
  }

  private FileLocation getFileLocationAnswer(InvocationOnMock invocation) {
    return JsonUtils.deepCopy(invocation.getArgument(0), FileLocation.class);
  }

  private FileLocation getFileLocation(Date createdDate) {
    String bucketName = RandomStringUtils.randomAlphanumeric(4);
    String folderName = USER_DES_ID + "/" + RandomStringUtils.randomAlphanumeric(9);
    String filename = TestUtils.getUuidString();

    String uri = String
        .format("%s%s/%s/%s", SRG_PROTOCOL, bucketName, folderName, filename);
    return FileLocation.builder()
        .fileID(filename)
        .driver(DriverType.GCS)
        .location(uri)
        .createdAt(createdDate)
        .createdBy(TEMP_USER)
        .build();
  }

  private LocationResponse getLocationResponseAnswer(InvocationOnMock invoc) {
    SignedUrl signedUrl = invoc.getArgument(0, SignedUrl.class);
    FileLocation fileLocation = invoc.getArgument(1, FileLocation.class);

    Map<String, String> location = new HashMap<>();
    location.put(SIGNED_URL_KEY, signedUrl.getUrl().toString());
    return LocationResponse.builder()
        .fileID(fileLocation.getFileID())
        .location(location)
        .build();
  }

  private Date toDate(LocalDateTime dateTime) {
    return Date.from(dateTime.toInstant(ZoneOffset.UTC));
  }

}
