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

package org.opengroup.osdu.workflow.provider.gcp.service;

import static com.google.api.client.http.HttpMethods.POST;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.IdTokenCredentials;
import com.google.auth.oauth2.IdTokenProvider;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.opengroup.osdu.workflow.exception.RuntimeException;
import org.opengroup.osdu.workflow.provider.gcp.exception.GoogleIamException;
import org.springframework.stereotype.Service;

@Service
public class GoogleIapHelper {

  static final String IAM_SCOPE = "https://www.googleapis.com/auth/iam";
  final HttpTransport httpTransport = new NetHttpTransport();

  /**
   * Fetch Google IAP client ID
   *
   * @param url service URL
   * @return IAP client ID
   */
  public String getIapClientId(String url) {
    try {
      Document doc = Jsoup.connect(url).get();

      String redirectLocation = doc.location();
      List<NameValuePair> queryParameters = URLEncodedUtils
          .parse(new URI(redirectLocation), StandardCharsets.UTF_8);

      return queryParameters.stream().filter(pair -> "client_id".equals(pair.getName())).findFirst()
          .orElseThrow(() -> new RuntimeException(
              String.format("No client_id found in redirect response to AirFlow - %s", url)))
          .getValue();
    } catch (IOException | URISyntaxException e) {
      throw new RuntimeException(
          "Exception during get Google IAP client id. Check link to Workflow engine", e);
    }
  }

  /**
   * Make request and add an IAP Bearer Authorization header with signed JWT token.
   */
  public HttpRequest buildIapRequest(String webServerUrl, String iapClientId,
      Map<String, Object> data) {
    try {
      JsonHttpContent jsonHttpContent = new JsonHttpContent(new JacksonFactory(), data);
      IdTokenProvider idTokenProvider = getIdTokenProvider();
      IdTokenCredentials credentials = IdTokenCredentials.newBuilder()
          .setIdTokenProvider(idTokenProvider)
          .setTargetAudience(iapClientId)
          .build();

      HttpRequestInitializer httpRequestInitializer = new HttpCredentialsAdapter(credentials);

      return httpTransport
          .createRequestFactory(httpRequestInitializer)
          .buildRequest(POST, new GenericUrl(webServerUrl), jsonHttpContent);
    } catch (IOException e) {
      throw new GoogleIamException("Exception when build authorized request", e);
    }
  }

  private IdTokenProvider getIdTokenProvider() throws IOException {
    GoogleCredentials credentials =
        GoogleCredentials.getApplicationDefault().createScoped(Collections.singleton(IAM_SCOPE));
    // service account credentials are required to sign the jwt token
    if (!(credentials instanceof IdTokenProvider)) {
      throw new GoogleIamException(
          "Google credentials : credentials that can provide id tokens expected");
    }
    return (IdTokenProvider) credentials;
  }

}
