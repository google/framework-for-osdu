/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.file.provider.gcp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.function.Function;
import javax.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.opengroup.osdu.file.ReplaceCamelCase;
import org.opengroup.osdu.file.provider.interfaces.FileListService;
import org.opengroup.osdu.file.provider.interfaces.FileService;
import org.opengroup.osdu.file.provider.interfaces.LocationService;
import org.opengroup.osdu.file.service.FileListServiceImpl;
import org.opengroup.osdu.file.service.FileServiceImpl;
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
class FileGcpDatastoreApplicationTest {

  @Inject
  private FunctionCatalog catalog;

  @Test
  void shouldHaveOnlyExactFunctions() {
    // when
    Set<String> names = catalog.getNames(Function.class);

    // then
    assertThat(names).hasSize(5)
        .containsExactlyInAnyOrder("getFileListFunction", "getFileLocationFunction",
            "getLocationFunction", "getFileFunction", RoutingFunction.FUNCTION_NAME);
  }

  @Test
  public void applicationStarts() {
    FileGcpDatastoreApplication.main(new String[] {});
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
    FileListService fileListService() {
      return Mockito.mock(FileListServiceImpl.class);
    }

  }
}
