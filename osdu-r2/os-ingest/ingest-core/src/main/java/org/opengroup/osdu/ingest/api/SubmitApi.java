/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.ingest.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.storage.StorageRole;
import org.opengroup.osdu.ingest.model.SubmitRequest;
import org.opengroup.osdu.ingest.model.SubmitResponse;
import org.opengroup.osdu.ingest.model.WorkProductLoadManifest;
import org.opengroup.osdu.ingest.provider.interfaces.IOsduSubmitService;
import org.opengroup.osdu.ingest.provider.interfaces.ISubmitService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestScope
@Validated
public class SubmitApi {

  final DpsHeaders headers;

  final ISubmitService submitService;
  final IOsduSubmitService osduSubmitService;

  // TODO: Create the permission for os-ingest and change pre authorize annotation
  @PostMapping("/submit")
  @PreAuthorize("@authorizationFilter.hasPermission('" + StorageRole.CREATOR + "')")
  public SubmitResponse submit(@RequestBody SubmitRequest submitRequest) {
    log.debug("Submit request received, with following headers : {}", headers);
    SubmitResponse submitResponse = submitService.submit(submitRequest, headers);
    log.debug("Submit result ready : {}", submitResponse);
    return submitResponse;
  }

  // TODO: Create the permission for os-ingest and change pre authorize annotation
  @PostMapping("/submitWithManifest")
  @PreAuthorize("@authorizationFilter.hasPermission('" + StorageRole.CREATOR + "')")
  public SubmitResponse submitWithManifest(@RequestBody WorkProductLoadManifest loadManifest) {
    log.debug("Submit with load manifest request received, with following headers : {}", headers);
    SubmitResponse submitResponse = osduSubmitService.submit(loadManifest, headers);
    log.debug("Submit load manifest result ready : {}", submitResponse);
    return submitResponse;
  }

}
