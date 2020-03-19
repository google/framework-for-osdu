// Copyright 2017-2019, Schlumberger
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.core.common.http;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.HttpResponse;
import org.opengroup.osdu.core.common.model.http.RequestStatus;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequestScope
public class HttpClientHandler {

    private final int RETRY_COUNT = 3;

    @Autowired
    @Lazy
    private JaxRsDpsLog log;

    private final RequestConfig REQUEST_CONFIG = RequestConfig.custom()
            .setConnectTimeout(60000)
            .setConnectionRequestTimeout(60000)
            .setSocketTimeout(60000).build();

    public HttpResponse sendRequest(HttpRequestBase request, DpsHeaders requestHeaders) {

        Long curTimeStamp = System.currentTimeMillis();

        List<Header> httpHeaders = new ArrayList<>();
        for (String key : requestHeaders.getHeaders().keySet()) {
            httpHeaders.add(new BasicHeader(key, requestHeaders.getHeaders().get(key)));
        }
        if (!requestHeaders.getHeaders().containsKey(HttpHeaders.ACCEPT)) {
            httpHeaders.add(new BasicHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON.toString()));
        }

        try {
            CloseableHttpClient httpclient = HttpClients.custom()
                    .setDefaultHeaders(httpHeaders)
                    .setDefaultRequestConfig(REQUEST_CONFIG)
                    .setServiceUnavailableRetryStrategy(getRetryStrategy()).build();
            try (CloseableHttpResponse response = httpclient.execute(request)) {

                StringBuilder responseBuilder = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
                    String responsePayloadLine;
                    while ((responsePayloadLine = br.readLine()) != null) {
                        responseBuilder.append(responsePayloadLine);
                    }
                }

                String responseBody = responseBuilder.toString();

                // handle case where upstream server is running out of resources and throwing generic exception
                checkResponseMediaType(response, responseBody);

                HttpResponse output = new HttpResponse();
                output.setResponseCode(response.getStatusLine().getStatusCode());
                output.setBody(responseBody);
                if (output.getResponseCode() != 200) {
                    log.info(String.format("method: %s | response code: %s | url: %s | error message: %s", request.getMethod(), output.getResponseCode(), request.getURI().toString(), responseBody));
                }
                return output;
            }
        } catch (SocketTimeoutException e) {
            throw new AppException(RequestStatus.SOCKET_TIMEOUT, "Socket time out", "Request cannot be completed in specified time", e);
        } catch (IOException e) {
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Internal communication failure", "Internal communication failure", e);
        } finally {
            Long latency = System.currentTimeMillis() - curTimeStamp;
            log.info(String.format("method: %s | latency: %s | url: %s | correlation id: %s", request.getMethod(), latency, request.getURI().toString(), requestHeaders.getHeaders().get(DpsHeaders.CORRELATION_ID)));
        }
    }

    private ServiceUnavailableRetryStrategy getRetryStrategy() {
        return new ServiceUnavailableRetryStrategy() {
            @Override
            public boolean retryRequest(
                    final org.apache.http.HttpResponse response, final int executionCount, final HttpContext context) {
                int statusCode = response.getStatusLine().getStatusCode();
                return statusCode >= 501 && executionCount <= RETRY_COUNT;
            }

            @Override
            public long getRetryInterval() {
                return 1000;
            }
        };
    }

    private boolean checkResponseMediaType(CloseableHttpResponse response, String responseBody) {
        try {
            String contentMimeType = ContentType.getOrDefault(response.getEntity()).getMimeType();
            if (ContentType.APPLICATION_JSON.getMimeType().equalsIgnoreCase(contentMimeType)) {
                return true;
            }
            throw new AppException(
                    HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE,
                    "Unsupported media type",
                    String.format("upstream server responded with unsupported media type: %s", contentMimeType),
                    String.format("upstream server response: %s", responseBody));
        } catch (ParseException | UnsupportedCharsetException e) {
            throw new AppException(
                    HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE,
                    "Unsupported media type",
                    "error parsing upstream server response entity content type",
                    String.format("upstream server response: %s", responseBody), e);
        }
    }
}