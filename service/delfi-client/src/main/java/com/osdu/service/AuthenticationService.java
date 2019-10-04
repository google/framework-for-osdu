package com.osdu.service;

public interface AuthenticationService {

  void checkCredentials(String authorizationToken, String partition);

}
