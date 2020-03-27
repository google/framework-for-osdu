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

package org.opengroup.osdu.delivery.provider.gcp.repository;

import static java.lang.String.format;
import static org.opengroup.osdu.delivery.provider.gcp.model.constant.StorageConstant.GCS_PROTOCOL;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.SignUrlOption;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.opengroup.osdu.delivery.model.SignedObject;
import org.opengroup.osdu.delivery.provider.interfaces.StorageRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.util.UriUtils;

@Repository
@Slf4j
@RequiredArgsConstructor
public class GcpStorageRepository implements StorageRepository {

  final Storage storage;

  @Override
  public SignedObject createSignedObject(String bucketName, String filepath) {
    log.debug("Creating the signed blob in bucket {} for path {}", bucketName, filepath);

    BlobId blobId = BlobId.of(bucketName, filepath);
    BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
        .setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE)
        .build();
    Blob blob = storage.create(blobInfo, ArrayUtils.EMPTY_BYTE_ARRAY);
    URL signedUrl = storage.signUrl(blobInfo, 7L, TimeUnit.DAYS,
        SignUrlOption.httpMethod(HttpMethod.PUT),
        SignUrlOption.withV4Signature());

    log.debug("Signed URL for created storage object. Object ID : {} , Signed URL : {}",
        blob.getGeneratedId(), signedUrl);
    return SignedObject.builder()
        .uri(getObjectUri(blob))
        .url(signedUrl)
        .build();
  }

  private URI getObjectUri(Blob blob) {
    String filepath = UriUtils.encodePath(blob.getName(), StandardCharsets.UTF_8);
    return URI.create(format("%s%s/%s", GCS_PROTOCOL, blob.getBucket(), filepath));
  }

}
