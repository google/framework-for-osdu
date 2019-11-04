package com.osdu.service.delfi;

import com.osdu.client.DelfiEntitlementsClient;
import com.osdu.exception.OsduUnauthorizedException;
import com.osdu.model.delfi.entitlement.UserGroups;
import com.osdu.model.property.DelfiPortalProperties;
import com.osdu.service.AuthenticationService;
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

  private void checkPreconditions(String authorizationToken, String partition) {
    if (authorizationToken == null) {
      throw new OsduUnauthorizedException("Missing authorization token");
    }

    if (partition == null) {
      throw new OsduUnauthorizedException("Missing partition");
    }
  }

}

