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

import com.osdu.client.DelfiEntitlementsClient;
import com.osdu.exception.OsduUnauthorizedException;
import com.osdu.model.delfi.entitlement.Group;
import com.osdu.model.delfi.entitlement.UserGroups;
import com.osdu.model.property.DelfiPortalProperties;
import com.osdu.service.AuthenticationService;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DelfiAuthenticationService implements AuthenticationService {

  final DelfiEntitlementsClient delfiEntitlementsClient;

  final DelfiPortalProperties portalProperties;

  @Override
  public UserGroups getUserGroups(String authorizationToken, String partition) {
    log.debug("Fetch user groups: {}, {}, {}", authorizationToken, portalProperties.getAppKey(),
        partition);

    // Delfi client returns Internal server error if no authorization token, so we check it here
    checkPreconditions(authorizationToken, partition);

    UserGroups userGroups = delfiEntitlementsClient
        .getUserGroups(authorizationToken, portalProperties.getAppKey(), partition);

    log.debug("Finished fetching user groups. User groups: {}", userGroups.toString());

    return userGroups;
  }

  @Override
  public void checkAuthentication(String authorizationToken, String partition) {
    log.debug("Start checking authentication. Authorization: {}, App key: {}, partition: {}",
        authorizationToken, portalProperties.getAppKey(), partition);

    checkPreconditions(authorizationToken, partition);

    UserGroups userGroups = getUserGroups(authorizationToken, partition);

    if (userGroups == null || userGroups.getGroups().isEmpty()) {
      throw new OsduUnauthorizedException("Missing user groups");
    }

    log.debug("Finished checking authentication. User belongs to groups: {}", userGroups);
  }

  @Override
  public Map<String, String> getGroupEmailToName(String authorizationToken, String partition) {
    return delfiEntitlementsClient
        .getUserGroups(authorizationToken, portalProperties.getAppKey(), partition)
        .getGroups().stream()
        .collect(Collectors.toMap(Group::getName, Group::getEmail));
  }

  private void checkPreconditions(String authorizationToken, String partition) {
    if (authorizationToken == null) {
      throw new OsduUnauthorizedException("Missing authorization token");
    }

    if (partition == null) {
      throw new OsduUnauthorizedException("Missing partition");
    }
  }

}

