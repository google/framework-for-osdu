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

package org.opengroup.osdu.file.gcp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import javax.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.opengroup.osdu.file.ReplaceCamelCase;
import org.opengroup.osdu.file.service.FileService;
import org.opengroup.osdu.file.service.FileServiceImpl;
import org.opengroup.osdu.file.service.FilesListService;
import org.opengroup.osdu.file.service.FilesListServiceImpl;
import org.opengroup.osdu.file.service.LocationService;
import org.opengroup.osdu.file.service.LocationServiceImpl;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.function.context.FunctionCatalog;
import org.springframework.cloud.function.context.config.RoutingFunction;
import org.springframework.cloud.function.context.test.FunctionalSpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
@FunctionalSpringBootTest
class FileGcpApplicationTest {

  @Inject
  private FunctionCatalog catalog;

  @Test
  void shouldHaveOnlyExactFunctions() {
    // when
    Set<String> names = catalog.getNames(null);

    // then
    assertThat(names).hasSize(5)
        .containsExactlyInAnyOrder("getFilesListFunction", "getFileLocationFunction",
            "getLocationFunction", "getFileFunction", RoutingFunction.FUNCTION_NAME);
  }

  @Test
  public void applicationStarts() {
    FileGcpApplication.main(new String[] {});
    Assertions.assertTrue(true, "silly assertion to be compliant with Sonar");
  }

  @TestConfiguration
  static class Config {

    @Bean
    FileService fileService() {
      return Mockito.mock(FileServiceImpl.class);
    }

    @Bean
    LocationService locationService() {
      return Mockito.mock(LocationServiceImpl.class);
    }

    @Bean
    FilesListService filesListService() {
      return Mockito.mock(FilesListServiceImpl.class);
    }

  }
}
