/*
 * Copyright  2020 Google LLC
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

package com.osdu.core.airflow;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.IdTokenCredentials;
import com.google.auth.oauth2.IdTokenProvider;
import lombok.SneakyThrows;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleIapHelper {

    static final String IAM_SCOPE = "https://www.googleapis.com/auth/iam";
    final HttpTransport httpTransport = new NetHttpTransport();

    @SneakyThrows
    public String getIapClientId(String url) {
        try {
            Document doc = Jsoup.connect(url).get();

            String redirectLocation = doc.location();
            List<NameValuePair> queryParameters = URLEncodedUtils
                    .parse(new URI(redirectLocation), StandardCharsets.UTF_8);

            return queryParameters.stream().filter(pair -> "client_id".equals(pair.getName())).findFirst()
                    .orElseThrow(() -> new Exception(
                            String.format("No client_id found in redirect response to AirFlow - %s", url)))
                    .getValue();
        } catch (IOException | URISyntaxException e) {
            throw new Exception("Exception during get Google IAP client id", e);
        }
    }

    /**
     * Make request and add an IAP Bearer Authorization header with signed JWT token.
     */
    @SneakyThrows
    public HttpRequest buildIapRequest(String webServerUrl, String iapClientId) {
        try {
            IdTokenProvider idTokenProvider = getIdTokenProvider();
            IdTokenCredentials credentials = IdTokenCredentials.newBuilder()
                    .setIdTokenProvider(idTokenProvider)
                    .setTargetAudience(iapClientId)
                    .build();

            HttpRequestInitializer httpRequestInitializer = new HttpCredentialsAdapter(credentials);

            return httpTransport
                    .createRequestFactory(httpRequestInitializer)
                    .buildGetRequest(new GenericUrl(webServerUrl));
        } catch (IOException e) {
            throw new Exception("Exception when build authorized request", e);
        }
    }

    @SneakyThrows
    private IdTokenProvider getIdTokenProvider() throws IOException {
        GoogleCredentials credentials =
                GoogleCredentials.getApplicationDefault().createScoped(Collections.singleton(IAM_SCOPE));
        // service account credentials are required to sign the jwt token
        if (!(credentials instanceof IdTokenProvider)) {
            throw new Exception(
                    "Google credentials : credentials that can provide id tokens expected");
        }
        return (IdTokenProvider) credentials;
    }
}