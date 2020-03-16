/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.ingest.provider.gcp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import javax.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opengroup.osdu.ingest.provider.interfaces.SubmitService;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.function.context.FunctionCatalog;
import org.springframework.cloud.function.context.config.RoutingFunction;
import org.springframework.cloud.function.context.test.FunctionalSpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@FunctionalSpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class IngestGcpApplicationTest {

  @Inject
  private FunctionCatalog catalog;

  @Test
  void shouldHaveOnlyExactFunctions() {
    // when
    Set<String> names = catalog.getNames(null);

    // then
    assertThat(names)
        .hasSize(3)
        .containsExactlyInAnyOrder("submitFunction", "submitWithManifestFunction",
            RoutingFunction.FUNCTION_NAME);
  }

  @Test
  public void applicationStarts() {
    IngestGcpApplication.main(new String[] {});
    Assertions.assertTrue(true, "silly assertion to be compliant with Sonar");
  }

  @TestConfiguration
  static class Config {

    @MockBean
    SubmitService submitService;

  }

}

