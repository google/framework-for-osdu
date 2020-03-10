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

package org.opengroup.osdu.delivery.client;

import static org.opengroup.osdu.core.common.model.Headers.AUTHORIZATION;

import org.opengroup.osdu.delivery.model.Headers;
import org.opengroup.osdu.delivery.model.entitlement.UserGroups;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(url = "${osdu.portal.url}/entitlements/v1",
    name = "entitlements.client")
public interface EntitlementsClient {

  //TODO: Add to dps headers once they are merged into core-common
  @GetMapping("/groups")
  UserGroups getUserGroups(@RequestHeader(AUTHORIZATION) String authorizationToken,
      @RequestHeader(Headers.APP_KEY) String applicationKey,
      @RequestHeader(Headers.SLB_DATA_PARTITION_ID) String partition);

}
