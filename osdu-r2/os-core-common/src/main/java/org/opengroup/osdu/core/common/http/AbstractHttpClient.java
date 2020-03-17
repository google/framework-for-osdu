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

package org.opengroup.osdu.core.common.http;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

abstract class AbstractHttpClient implements IHttpClient {

    @Override
    public HttpResponse send(HttpRequest request) {

        HttpResponse output = new HttpResponse();
        output.setRequest(request);
        HttpURLConnection conn = null;
        try {
            request.setUrl(encodeUrl(request.getUrl()));

            long start = System.currentTimeMillis();
            conn = this.createConnection(request);
            this.sendRequest(conn, request.body);

            output.setResponseCode(conn.getResponseCode());
            output.setContentType(conn.getContentType());
            output.setHeaders(conn.getHeaderFields());

            if (output.isSuccessCode()) {
                output.setBody(getBody(conn.getInputStream()).toString());

            } else {
                output.setBody(getBody(conn.getErrorStream()).toString());
            }

            output.setLatency(System.currentTimeMillis() - start);
        } catch (IOException e) {
            System.err.println(String.format("Unexpected error sending to URL %s METHOD %s. error %s", request.url,
                    request.httpMethod, e));
            output.setException(e);
        } catch (URISyntaxException e) {
            output.setException(e);
        } finally {
            if (conn != null)
                conn.disconnect();
        }

        return output;
    }

    private StringBuilder getBody(InputStream stream) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(stream))) {
            String inputLine;
            StringBuilder resp = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                resp.append(inputLine);
            }
            return resp;
        }
    }

    HttpURLConnection createConnection(HttpRequest request)
            throws IOException {

        HttpURLConnection conn = null;

        URL url = new URL(request.url);
        conn = (HttpURLConnection) url.openConnection();
        conn.setInstanceFollowRedirects(request.followRedirects);
        conn.setConnectTimeout(request.connectionTimeout);

        request.headers.forEach(conn::setRequestProperty);

        if (request.httpMethod.equals(HttpRequest.POST) ||
                request.httpMethod.equals(HttpRequest.PUT) ||
                request.httpMethod.equals(HttpRequest.PATCH)) {
            conn.setDoOutput(true); //only set if we have a body on request
        }
        conn.setRequestMethod(request.httpMethod);

        return conn;
    }

    private void sendRequest(HttpURLConnection connection, String body) throws IOException {
        if (!StringUtils.isBlank(body)) {
            try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream())) {
                writer.write(body);
            }
        }
    }

    private String encodeUrl(String url) throws MalformedURLException, URISyntaxException {
        URL temp = new URL(url);
        URI uri = new URI(temp.getProtocol(), temp.getUserInfo(), temp.getHost(), temp.getPort(),
                temp.getPath(), temp.getQuery(), temp.getRef());
        return uri.toASCIIString();
    }

}
