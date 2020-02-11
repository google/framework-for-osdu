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

package org.opengroup.osdu.file.gcp.repository;

import com.google.api.client.testing.util.SecurityTestUtils;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.auth.oauth2.UserCredentials;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import lombok.SneakyThrows;

public final class TestCredential {

  private static final String SA_CLIENT_EMAIL = "test-user@test-project.iam.gserviceaccount.com";
  private static final String SA_CLIENT_ID = "test-user";
  private static final String SA_PRIVATE_KEY_ID = "d84a4fefcf50791d4a90f2d7af17469d6282df9d";
  private static final String SA_PRIVATE_KEY_PKCS8;
  private static final Collection<String> EMPTY_SCOPES = Collections.emptyList();

  private static final String CLIENT_SECRET = "jakuaL9YyieakhECKL2SwZcu";
  private static final String CLIENT_ID = "ya29.1.AADtN_UtlxN3PuGAxrN2XQnZTVRvDyVWnYq4I6dws";
  private static final String REFRESH_TOKEN = "1/Tl6awhpFjkMkSJoj1xsli0H2eL5YsMgU_NKPY2TyGWY";

  static {
    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(SecurityTestUtils.newEncodedRsaPrivateKeyBytes());
    SA_PRIVATE_KEY_PKCS8 = "-----BEGIN PRIVATE KEY-----\n"
        + new String(Base64.getEncoder().encode(keySpec.getEncoded()))
        + "\n-----END PRIVATE KEY-----\n";
  }

  private TestCredential() {
  }

  @SneakyThrows
  static ServiceAccountCredentials getSa() {
    return ServiceAccountCredentials.fromPkcs8(
        SA_CLIENT_ID, SA_CLIENT_EMAIL, SA_PRIVATE_KEY_PKCS8, SA_PRIVATE_KEY_ID, EMPTY_SCOPES);
  }

  static UserCredentials getUserCredentials() {
    return UserCredentials.newBuilder()
        .setClientId(CLIENT_ID)
        .setClientSecret(CLIENT_SECRET)
        .setRefreshToken(REFRESH_TOKEN)
        //.setQuotaProjectId(QUOTA_PROJECT)
        .build();
  }

}
