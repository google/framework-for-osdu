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

package org.opengroup.osdu.delivery.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.opengroup.osdu.delivery.TestUtils.AUTHORIZATION_TOKEN;
import static org.opengroup.osdu.delivery.TestUtils.PARTITION;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.delivery.ReplaceCamelCase;
import org.opengroup.osdu.delivery.exception.OsduUnauthorizedException;
import org.opengroup.osdu.delivery.provider.interfaces.AuthenticationService;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
class AuthenticationServiceImplTest {

  private AuthenticationService authenticationService = new AuthenticationServiceImpl(null);

  @Test
  @Disabled
  void shouldCheckAuthentication() {
    // when
    Throwable thrown = catchThrowable(() -> authenticationService.checkAuthentication(
        AUTHORIZATION_TOKEN, PARTITION));

    // then
    assertThat(thrown).isNull();
  }

  @Test
  @Disabled
  void shouldThrowWhenNothingIsSpecified() {
    // when
    Throwable thrown = catchThrowable(() -> authenticationService.checkAuthentication(
        null, null));

    // then
    assertThat(thrown)
        .isInstanceOf(OsduUnauthorizedException.class);
  }

  @Test
  @Disabled
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
  @Disabled
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
