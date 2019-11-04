package com.osdu.service;

import com.osdu.exception.OsduUnauthorizedException;
import com.osdu.model.delfi.entitlement.UserGroups;

public interface AuthenticationService {

  /** Method returns groups that user belongs to.
   * It throws Unauthorized exception if authentication properties not valid.
   *
   * @param authorizationToken Bearer token
   * @param partition partition
   * @return groups that user belongs to
   */
  UserGroups getUserGroups(String authorizationToken, String partition);

  /** Check if authentication properties are valid.
   * It throws Unauthorized exception if authentication properties not valid.
   *
   * @param authorizationToken Bearer token
   * @param partition partition
   * @throws OsduUnauthorizedException if token and partition are missing, invalid
   *     or partition doesn't have assigned user groups
   */
  void checkAuthentication(String authorizationToken, String partition);

}
