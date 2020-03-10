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

package org.opengroup.osdu.delivery.provider.gcp;

import static java.lang.String.format;
import static org.opengroup.osdu.delivery.provider.gcp.model.constant.StorageConstant.GCS_PROTOCOL;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.SneakyThrows;
import org.assertj.core.api.Condition;

public final class TestUtils {

  public static final String AUTHORIZATION_TOKEN = "authToken";
  public static final String PARTITION = "partition";
  public static final String USER_DES_ID = "common-user";
  public static final String BUCKET_NAME = "odes-os-file-temp";

  public static final String UUID_REGEX = "(.{8})(.{4})(.{4})(.{4})(.{12})";
  public static final Pattern GCS_OBJECT_URI
      = Pattern.compile("^gs://[\\w,\\s-]+/[\\w,\\s-]+/[\\w,\\s-]+/[\\w,\\s-]+/?.*$");
  public static final Condition<String> UUID_CONDITION
          = new Condition<>(TestUtils::isValidUuid, "Valid UUID");
  public static final Condition<String> GCS_URL_CONDITION
              = new Condition<>(TestUtils::isValidSingedUrl, "Signed URL for GCS object");
  public static final String FILE_ID = "test-file-id.tmp";

  private TestUtils() {
  }

  private static boolean isValidUuid(String uuid) {
    try {
      String normalizedUuid = uuid.replaceAll(UUID_REGEX, "$1-$2-$3-$4-$5");
      UUID.fromString(normalizedUuid);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  private static boolean isValidSingedUrl(String url) {
    try {
      new URL(url);
      return true;
    } catch (MalformedURLException e) {
      return false;
    }
  }

  public static URI getGcsObjectUri(String bucketName, String folderName, String filename) {
    return URI.create(format("%s%s/%s/%s", GCS_PROTOCOL, bucketName, folderName, filename));
  }

  @SneakyThrows
  public static URL getGcsObjectUrl(String bucketName, String folderName, String filename) {
    return new URL(format(
        "https://storage.googleapis.com/%s/%s/%s?X-Goog-Algorithm=aaa&X-Goog-Credential=BBB",
        bucketName, folderName, filename));
  }

  public static Instant now() {
    return Instant.now(Clock.systemUTC());
  }

  public static String getUuidString() {
    return UUID.randomUUID().toString().replace("-", "");
  }

}
