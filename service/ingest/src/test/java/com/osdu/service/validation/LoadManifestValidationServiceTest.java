/*
 * Copyright 2019 Google LLC
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

package com.osdu.service.validation;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.BDDMockito.willThrow;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.osdu.ReplaceCamelCase;
import com.osdu.exception.IngestException;
import com.osdu.exception.OsduServerErrorException;
import com.osdu.model.type.base.ExtensionProperties;
import com.osdu.model.type.base.IndividualTypeProperties;
import com.osdu.model.type.file.FileData;
import com.osdu.model.type.file.FileGroupTypeProperties;
import com.osdu.model.type.manifest.LoadManifest;
import com.osdu.model.type.manifest.ManifestFile;
import com.osdu.model.type.manifest.ManifestWp;
import com.osdu.model.type.manifest.ManifestWpc;
import com.osdu.model.type.wp.WpData;
import com.osdu.model.type.wp.WpGroupTypeProperties;
import com.osdu.model.type.wp.WpIndividualTypeProperties;
import com.osdu.model.type.wp.WpcData;
import com.osdu.model.type.wp.WpcGroupTypeProperties;
import com.osdu.model.type.wp.WpcIndividualTypeProperties;
import com.osdu.service.processing.CustomSchemeFetcher;
import com.osdu.service.validation.schema.ClassloaderManifestSchemaReceiver;
import com.osdu.service.validation.schema.ManifestSchemaReceiver;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
class LoadManifestValidationServiceTest {

  private static final String FILEPATH = "http://some.host.com/temp-file/file.las";
  private static final String WP_RESOURCE_TYPE_ID = "srn:type:work-product/WellLog:version1";
  private static final String WPC_RESOURCE_TYPE_ID = "srn:type:work-product-component/WellLog:version1";
  private static final String FILE_RESOURCE_TYPE_ID = "srn:type:file/las2:version1";
  private static final String RESOURCE_SECURITY_CLASSIFICATION =
      "srn:reference-data/ResourceSecurityClassification:RESTRICTED:";
  private static final String WPC_ID_1 = "wpc-1";
  private static final String FILE_ID_1 = "f-1";
  private static final String WELL_LOG_NAME = "AKM-11 LOG";
  private static final String DESCRIPTION = "Well Log";

  @Mock
  private CustomSchemeFetcher schemeFetcher;
  @Spy
  private ManifestSchemaReceiver manifestSchemaReceiver = new ClassloaderManifestSchemaReceiver();

  private JsonValidationService jsonValidationService;
  private ObjectMapper objectMapper;

  private LoadManifestValidationService service;

  @BeforeEach
  void setUp() {
    jsonValidationService = Mockito.spy(new JsonValidationService(schemeFetcher));
    objectMapper = Mockito.spy(new ObjectMapper()
        .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
        .setSerializationInclusion(Include.NON_NULL)
        .findAndRegisterModules());
    service = new LoadManifestValidationService(manifestSchemaReceiver, jsonValidationService,
        objectMapper);
  }

  @Test
  void shouldSuccessfullyValidateLoadManifest() {
    // when
    service.validateManifest(getLoadManifest());

    // then
    InOrder inOrder = Mockito.inOrder(jsonValidationService, schemeFetcher);
    inOrder.verify(jsonValidationService).validate(any(JsonNode.class), any(JsonNode.class));
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void shouldThrowIngestExceptionWhenValidationHasTheErrors() {
    // when
    Throwable thrown = catchThrowable(() -> service.validateManifest(new LoadManifest()));

    // then
    then(thrown)
        .isInstanceOf(IngestException.class)
        .hasMessageStartingWith("Failed to validate json from manifest")
        .hasMessageContaining("validation result is");

    InOrder inOrder = Mockito.inOrder(jsonValidationService, schemeFetcher);
    inOrder.verify(jsonValidationService).validate(any(JsonNode.class), any(JsonNode.class));
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void shouldThrowServerErrorExceptionWhen() {
    // given
    willReturn(null).given(manifestSchemaReceiver).getLoadManifestSchema();

    // when
    Throwable thrown = catchThrowable(() -> service.validateManifest(getLoadManifest()));

    // then
    then(thrown)
        .isInstanceOf(OsduServerErrorException.class)
        .hasMessage("Can not find resource for load manifest schema");

    InOrder inOrder = Mockito.inOrder(jsonValidationService, schemeFetcher);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void shouldThrowIngestExceptionWhenFailToParseManifestSchema() throws IOException {
    // given
    willThrow(IOException.class).given(objectMapper).readTree(any(InputStream.class));

    // when
    Throwable thrown = catchThrowable(() -> service.validateManifest(getLoadManifest()));

    // then
    then(thrown)
        .isInstanceOf(IngestException.class)
        .hasMessageStartingWith("Fail parse load manifest");

    InOrder inOrder = Mockito.inOrder(jsonValidationService, schemeFetcher);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void shouldThrowIllegalArgumentExceptionWhenUnableToReadLoadManifestAsTree() {
    // given
    willThrow(IllegalArgumentException.class).given(objectMapper)
        .valueToTree(any(LoadManifest.class));

    // when
    Throwable thrown = catchThrowable(() -> service.validateManifest(getLoadManifest()));

    // then
    then(thrown).isInstanceOf(IllegalArgumentException.class);

    InOrder inOrder = Mockito.inOrder(jsonValidationService, schemeFetcher);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void shouldThrowIngestExceptionWhenFailToCreateJsonSchema() throws IOException {
    // given
    JsonNode uriNode = Mockito.mock(JsonNode.class);
    JsonNode schemaNode = Mockito.mock(JsonNode.class);

    willReturn(schemaNode).given(objectMapper).readTree(any(InputStream.class));
    given(schemaNode.get("$schema")).willReturn(uriNode);

    // when
    Throwable thrown = catchThrowable(() -> service.validateManifest(getLoadManifest()));

    // then
    then(thrown)
        .isInstanceOf(IngestException.class)
        .hasMessageStartingWith("Error creating json validation schema from json object:");

    InOrder inOrder = Mockito.inOrder(jsonValidationService, schemeFetcher);
    inOrder.verify(jsonValidationService).validate(any(JsonNode.class), any(JsonNode.class));
    inOrder.verifyNoMoreInteractions();
  }

  private LoadManifest getLoadManifest() {
    ManifestFile file = ManifestFile.builder()
        .resourceTypeID(FILE_RESOURCE_TYPE_ID)
        .resourceSecurityClassification(RESOURCE_SECURITY_CLASSIFICATION)
        .associativeId(FILE_ID_1)
        .data(FileData.builder()
            .groupTypeProperties(FileGroupTypeProperties.builder()
                .preLoadFilePath(FILEPATH)
                .fileSource("")
                .build())
            .individualTypeProperties(new IndividualTypeProperties())
            .extensionProperties(new ExtensionProperties())
            .build())
        .build();

    ManifestWpc wpc = ManifestWpc.builder()
        .resourceTypeID(WPC_RESOURCE_TYPE_ID)
        .resourceSecurityClassification(RESOURCE_SECURITY_CLASSIFICATION)
        .associativeId(WPC_ID_1)
        .fileAssociativeIds(Collections.singletonList(FILE_ID_1))
        .data(WpcData.builder()
            .groupTypeProperties(WpcGroupTypeProperties.builder()
                .files(Collections.emptyList())
                .artefacts(Collections.emptyList())
                .build())
            .individualTypeProperties(WpcIndividualTypeProperties.builder()
                .name(WELL_LOG_NAME)
                .description(DESCRIPTION)
                .build())
            .extensionProperties(new ExtensionProperties())
            .build())
        .build();

    ManifestWp wp = ManifestWp.builder()
        .resourceTypeID(WP_RESOURCE_TYPE_ID)
        .resourceSecurityClassification(RESOURCE_SECURITY_CLASSIFICATION)
        .componentsAssociativeIDs(Collections.singletonList(WPC_ID_1))
        .data(WpData.builder()
            .groupTypeProperties(WpGroupTypeProperties.builder()
                .components(Collections.emptyList())
                .build())
            .individualTypeProperties(WpIndividualTypeProperties.builder()
                .name(WELL_LOG_NAME)
                .description(DESCRIPTION)
                .build())
            .extensionProperties(new ExtensionProperties())
            .build())
        .build();

    return LoadManifest.builder()
        .workProduct(wp)
        .workProductComponent(wpc)
        .file(file)
        .build();
  }
}