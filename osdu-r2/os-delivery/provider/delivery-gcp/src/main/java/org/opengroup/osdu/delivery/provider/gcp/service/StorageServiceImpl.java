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

package org.opengroup.osdu.delivery.provider.gcp.service;

import static java.lang.String.format;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.delivery.exception.OsduBadRequestException;
import org.opengroup.osdu.delivery.model.SignedObject;
import org.opengroup.osdu.delivery.model.SignedUrl;
import org.opengroup.osdu.delivery.provider.gcp.model.constant.StorageConstant;
import org.opengroup.osdu.delivery.provider.gcp.model.property.FileLocationProperties;
import org.opengroup.osdu.delivery.provider.interfaces.StorageRepository;
import org.opengroup.osdu.delivery.provider.interfaces.StorageService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {

  private static final DateTimeFormatter DATE_TIME_FORMATTER
      = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss-SSS");

  final FileLocationProperties fileLocationProperties;
  final StorageRepository storageRepository;

  @Override
  public SignedUrl createSignedUrl(String fileID, String authorizationToken, String partitionID) {
    log.debug("Creating the signed blob for fileID : {}. Authorization : {}, partitionID : {}",
        fileID, authorizationToken, partitionID);
    Instant now = Instant.now(Clock.systemUTC());

    String bucketName = getBucketName(partitionID);
    String userDesID = getUserDesID(authorizationToken);
    String filepath = getFileLocationPrefix(now, fileID, userDesID);
    log.debug("Create storage object for fileID {} in bucket {} with filepath {}",
        fileID, bucketName, filepath);

    if (filepath.length() > StorageConstant.GCS_MAX_FILEPATH) {
      throw new OsduBadRequestException(format(
          "The maximum filepath length is %s characters, but got a name with %s characters",
          StorageConstant.GCS_MAX_FILEPATH, filepath.length()));
    }

    SignedObject signedObject = storageRepository.createSignedObject(bucketName, filepath);

    return SignedUrl.builder()
        .url(signedObject.getUrl())
        .uri(signedObject.getUri())
        .createdBy(userDesID)
        .createdAt(now)
        .build();
  }

  private String getBucketName(String partitionID) {
    return fileLocationProperties.getBucketName();
  }

  private String getUserDesID(String authorizationToken) {
    return fileLocationProperties.getUserId();
  }

  private String getFileLocationPrefix(Instant instant, String filename, String userDesID) {
    String folderName = instant.toEpochMilli() + "-"
        + DATE_TIME_FORMATTER
        .withZone(ZoneOffset.UTC)
        .format(instant);

    return format("%s/%s/%s", userDesID, folderName, filename);
  }

}
