package com.osdu.service.delfi;

import com.osdu.client.DelfiEntitlementsClient;
import com.osdu.service.AuthenticationService;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DelfiAuthenticationService implements AuthenticationService {

  @Inject
  DelfiEntitlementsClient delfiEntitlementsClient;

  @Value("${osdu.delfi.portal.appkey}")
  String appKey;

  @Override
  public void checkCredentials(String authorizationToken, String partition) {
    log.debug("Start authentication : {}, {}, {}", authorizationToken, appKey, partition);

    delfiEntitlementsClient
        .getUserGroups(authorizationToken, appKey, partition);

    log.debug("Authentication finished");
  }
}

