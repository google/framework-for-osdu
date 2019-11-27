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

import static java.lang.String.format;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.storage.Blob;
import com.osdu.client.DelfiIngestionClient;
import com.osdu.model.Record;
import com.osdu.model.RequestContext;
import com.osdu.model.delfi.signed.SignedFile;
import com.osdu.model.delfi.signed.SignedUrlResult;
import com.osdu.model.property.DelfiPortalProperties;
import com.osdu.model.type.manifest.ManifestFile;
import com.osdu.service.IngestionService;
import com.osdu.service.PortalService;
import com.osdu.service.SrnMappingService;
import com.osdu.service.StorageService;
import com.osdu.service.google.GcpSrnMappingService;
import com.osdu.service.helper.IngestionHelper;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DelfiIngestionService implements IngestionService {

  final DelfiPortalProperties portalProperties;
  final DelfiIngestionClient delfiIngestionClient;
  final SrnMappingService srnMappingService;
  final StorageService storageService;
  final PortalService portalService;
  final DelfiPortalService delfiPortalService;
  final ObjectMapper mapper;
  final GcpSrnMappingService gcpSrnMappingService;
  final IngestionHelper ingestionHelper;

  @Override
  public SignedFile uploadFile(ManifestFile file, String authorizationToken, String partition) {
    URL url = ingestionHelper.createUrlFromManifestFile(file);
    SignedUrlResult result = transferFile(url, authorizationToken, partition);

    return SignedFile.builder()
        .file(file)
        .locationUrl(result.getLocationUrl())
        .relativeFilePath(result.getRelativeFilePath())
        .build();
  }

  @Override
  public SignedUrlResult transferFile(URL fileUrl, String authToken, String partition) {
    String fileName = ingestionHelper.getFileNameFromUrl(fileUrl);
    Blob blob = storageService.uploadFileToStorage(fileUrl, fileName);

    SignedUrlResult signedUrlResult = delfiIngestionClient
        .getSignedUrlForLocation(fileName, authToken, portalProperties.getAppKey(), partition);

    storageService.writeFileToSignedUrlLocation(blob, signedUrlResult.getLocationUrl());
    return signedUrlResult;
  }

  @Override
  public List<Record> failRecords(List<Record> records, RequestContext requestContext) {
    return records.stream()
        .map(record -> failRecord(requestContext, record))
        .collect(Collectors.toList());
  }

  private Record failRecord(RequestContext requestContext, Record record) {
    log.debug(format("Fail delfi record : %s", record.getId()));
    record.getData().put("ResourceLifecycleStatus",
        "srn:reference-data/ResourceLifecycleStatus:RESCINDED:");
    portalService.putRecord(record, requestContext.getAuthorizationToken(),
        requestContext.getPartition());
    return record;
  }

}
