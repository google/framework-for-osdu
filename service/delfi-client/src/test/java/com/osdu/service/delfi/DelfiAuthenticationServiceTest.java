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

package com.osdu.service.delfi;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.osdu.client.DelfiEntitlementsClient;
import com.osdu.exception.OsduException;
import com.osdu.exception.OsduUnauthorizedException;
import com.osdu.model.delfi.entitlement.Group;
import com.osdu.model.delfi.entitlement.UserGroups;
import com.osdu.model.property.DelfiPortalProperties;
import java.util.Collections;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DelfiAuthenticationServiceTest {

  private static final String APP_KEY = "appKey";
  private static final String PARTITION = "partition";
  private static final String AUTH = "auth";

  @Mock
  private DelfiEntitlementsClient delfiEntitlementsClient;
  @Mock
  private DelfiPortalProperties delfiPortalProperties;
  @InjectMocks
  private DelfiAuthenticationService delfiAuthenticationService;

  @Test
  public void shouldGetUserGroups() {

    // given
    UserGroups expectedUserGroups = new UserGroups();
    when(delfiPortalProperties.getAppKey()).thenReturn(APP_KEY);
    when(delfiEntitlementsClient.getUserGroups(eq(AUTH), eq(APP_KEY), eq(PARTITION))).thenReturn(expectedUserGroups);

    // when
    UserGroups userGroups = delfiAuthenticationService.getUserGroups(AUTH, PARTITION);

    // then
    assertEquals(expectedUserGroups, userGroups);
  }

  @Test
  public void shouldFailIfNoAuthToken() {

    // given
    when(delfiPortalProperties.getAppKey()).thenReturn(APP_KEY);
    String authToken = null;

    // when
    Throwable thrown = catchThrowable(
        () -> delfiAuthenticationService.getUserGroups(authToken, PARTITION));

    // then
    Assertions.assertThat(thrown)
        .isInstanceOf(OsduException.class)
        .hasMessageContaining("Missing authorization token");
  }

  @Test
  public void shouldCheckAuthentication() {

    // given
    UserGroups userGroups = new UserGroups();
    userGroups.setGroups(Collections.singletonList(new Group()));

    when(delfiPortalProperties.getAppKey()).thenReturn(APP_KEY);
    when(delfiEntitlementsClient.getUserGroups(eq(AUTH), eq(APP_KEY), eq(PARTITION))).thenReturn(userGroups);

    // when
    delfiAuthenticationService.checkAuthentication(AUTH, PARTITION);
  }

  @Test
  public void shouldFailCheckAuthenticationIfThereIsNoUserGroups() {

    // given
    when(delfiPortalProperties.getAppKey()).thenReturn(APP_KEY);
    when(delfiEntitlementsClient.getUserGroups(eq(AUTH), eq(APP_KEY), eq(PARTITION))).thenReturn(null);

    // when
    Throwable thrown = catchThrowable(
        () -> delfiAuthenticationService.checkAuthentication(AUTH, PARTITION));

    // then
    Assertions.assertThat(thrown)
        .isInstanceOf(OsduUnauthorizedException.class)
        .hasMessageContaining("Missing user groups");
  }
}
