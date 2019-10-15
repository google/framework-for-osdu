package com.osdu.service.delfi;

import com.osdu.client.DelfiEntitlementsClient;
import com.osdu.exception.OsduUnauthorizedException;
import com.osdu.model.delfi.entitlement.UserGroups;
import com.osdu.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DelfiAuthenticationService implements AuthenticationService {

  final DelfiEntitlementsClient delfiEntitlementsClient;

  @Value("${search.mapper.delfi.appkey}")
  String applicationKey;

  @Override
  public UserGroups checkAuthentication(String authorizationToken, String partition) {
    log.debug("Start authentication : {}, {}, {}", authorizationToken, applicationKey,
        partition);

    // Delfi client returns Internal server error if no authorization token, so we check it here
    if (authorizationToken == null) {
      throw new OsduUnauthorizedException("Unauthorized");
    }

    UserGroups userGroups = delfiEntitlementsClient
        .getUserGroups(authorizationToken, applicationKey, partition);

    log.debug("Authentication finished. User belongs to groups: " + userGroups.toString());

    return userGroups;
  }
}
