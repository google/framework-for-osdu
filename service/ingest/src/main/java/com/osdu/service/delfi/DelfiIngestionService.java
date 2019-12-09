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

import com.google.cloud.storage.Blob;
import com.osdu.client.DelfiIngestionClient;
import com.osdu.client.delfi.RecordDataFields;
import com.osdu.exception.IngestException;
import com.osdu.model.Record;
import com.osdu.model.RequestContext;
import com.osdu.model.delfi.signed.SignedFile;
import com.osdu.model.delfi.signed.SignedUrlResult;
import com.osdu.model.property.DelfiPortalProperties;
import com.osdu.model.type.base.OsduObject;
import com.osdu.model.type.manifest.ManifestFile;
import com.osdu.service.IngestionService;
import com.osdu.service.JsonUtils;
import com.osdu.service.PortalService;
import com.osdu.service.StorageService;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DelfiIngestionService implements IngestionService {

  final DelfiPortalProperties portalProperties;
  final DelfiIngestionClient delfiIngestionClient;
  final StorageService storageService;
  final PortalService portalService;

  @Override
  public SignedFile uploadFile(ManifestFile file, String authorizationToken, String partition) {
    URL url = createUrlFromManifestFile(file);
    SignedUrlResult result = transferFile(url, authorizationToken, partition);

    return SignedFile.builder()
        .file(file)
        .locationUrl(result.getLocationUrl())
        .relativeFilePath(result.getRelativeFilePath())
        .build();
  }

  private SignedUrlResult transferFile(URL fileUrl, String authToken, String partition) {
    String fileName = getFileNameFromUrl(fileUrl);
    Blob blob = storageService.uploadFileToStorage(fileUrl, fileName);

    SignedUrlResult signedUrlResult = delfiIngestionClient
        .getSignedUrlForLocation(fileName, authToken, portalProperties.getAppKey(), partition);

    if (signedUrlResult.getResponseCode() != HttpStatus.CREATED.value()) {
      throw new IngestException("Count not fetch a signed URL to landing zone for file: "
          + fileName);
    }

    storageService.writeFileToSignedUrlLocation(blob, signedUrlResult.getLocationUrl());
    return signedUrlResult;
  }

  @Override
  public List<Record> failRecords(List<Record> records, RequestContext requestContext) {
    return records.stream()
        .map(record -> failRecord(requestContext, record))
        .collect(Collectors.toList());
  }

  private static URL createUrlFromManifestFile(ManifestFile file) {
    String preLoadFilePath = file.getData().getGroupTypeProperties().getPreLoadFilePath();
    try {
      return new URL(preLoadFilePath);
    } catch (MalformedURLException e) {
      throw new IngestException(
          format("Could not create URL from preload file path: %s", preLoadFilePath),
          e);
    }
  }

  /**
   * Returns file name from URL. Is used to get file name from signed URL.
   */
  private static String getFileNameFromUrl(URL fileUrl) {
    try {
      Path filePath = Paths.get(new URI(fileUrl.toString()).getPath()).getFileName();
      final String fileName = filePath == null ? null : filePath.toString();
      if (StringUtils.isEmpty(fileName)) {
        throw new IngestException(format("File name obtained is empty, URL : %s", fileUrl));
      }
      return fileName;
    } catch (URISyntaxException e) {
      throw new IngestException(format("Can not get file name from URL: %s", fileUrl), e);
    }
  }

  private Record failRecord(RequestContext requestContext, Record record) {
    log.debug(format("Fail delfi record : %s", record.getId()));
    OsduObject osduObject = JsonUtils.deepCopy(record.getData().get(RecordDataFields.OSDU_DATA),
        OsduObject.class);
    osduObject.setResourceLifecycleStatus("srn:reference-data/ResourceLifecycleStatus:RESCINDED:");
    record.getData().put(RecordDataFields.OSDU_DATA, osduObject);
    return portalService.putRecord(record, requestContext.getAuthorizationToken(),
        requestContext.getPartition());
  }

}
