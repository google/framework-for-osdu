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
