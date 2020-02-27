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

package org.opengroup.osdu.workflow.provider.gcp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.exception.OsduUnauthorizedException;
import org.opengroup.osdu.workflow.ReplaceCamelCase;
import org.opengroup.osdu.workflow.provider.interfaces.AuthenticationService;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
class GcpAuthenticationServiceTest {

  private AuthenticationService authenticationService = new GcpAuthenticationService();
  private static final String AUTHORIZATION_TOKEN = "authToken";
  private static final String PARTITION = "partition";

  @Test
  void shouldCheckAuthentication() {
    // when
    Throwable thrown = catchThrowable(() -> authenticationService.checkAuthentication(
        AUTHORIZATION_TOKEN, PARTITION));

    // then
    assertThat(thrown).isNull();
  }

  @Test
  void shouldThrowWhenNothingIsSpecified() {
    // when
    Throwable thrown = catchThrowable(() -> authenticationService.checkAuthentication(
        null, null));

    // then
    assertThat(thrown)
        .isInstanceOf(OsduUnauthorizedException.class);
  }

  @Test
  void shouldThrowWhenOnlyTokenIsSpecified() {
    // when
    Throwable thrown = catchThrowable(() -> authenticationService.checkAuthentication(
        AUTHORIZATION_TOKEN, null));

    // then
    assertThat(thrown)
        .isInstanceOf(OsduUnauthorizedException.class)
        .hasMessage("Missing partitionID");
  }

  @Test
  void shouldThrowWhenOnlyPartitionIsSpecified() {
    // when
    Throwable thrown = catchThrowable(() -> authenticationService.checkAuthentication(
        null, PARTITION));

    // then
    assertThat(thrown)
        .isInstanceOf(OsduUnauthorizedException.class)
        .hasMessage("Missing authorization token");
  }

}
