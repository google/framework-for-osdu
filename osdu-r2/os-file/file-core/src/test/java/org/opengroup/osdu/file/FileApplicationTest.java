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

package org.opengroup.osdu.file;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opengroup.osdu.file.service.FileListService;
import org.opengroup.osdu.file.service.FileService;
import org.opengroup.osdu.file.service.LocationService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.function.context.FunctionCatalog;
import org.springframework.cloud.function.context.config.RoutingFunction;
import org.springframework.cloud.function.context.test.FunctionalSpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@FunctionalSpringBootTest
class FileApplicationTest {

  @Inject
  private FunctionCatalog catalog;

  @Test
  void shouldHaveOnlyExactFunctions() {
    // when
    Set<String> names = catalog.getNames(null);

    // then
    assertThat(names).hasSize(5)
        .containsExactlyInAnyOrder("getFileListFunction", "getFileLocationFunction",
            "getLocationFunction", "getFileFunction", RoutingFunction.FUNCTION_NAME);
  }

  @TestConfiguration
  static class Config {

    @MockBean
    FileService fileService;

    @MockBean
    LocationService locationService;

    @MockBean
    FileListService fileListService;

  }

}
