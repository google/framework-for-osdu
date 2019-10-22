package com.osdu.service;

import com.osdu.model.delfi.entitlement.UserGroups;

public interface AuthenticationService {

  /** Method returns groups that user belongs to.
   * Can be used for check if authentication properties are valid.
   * It throws Unauthorized exception if authentication properties not valid.
   *
   * @param authorizationToken Bearer token
   * @param partition partition
   * @return groups that user belongs to
   */
  UserGroups checkAuthentication(String authorizationToken, String partition);

}
