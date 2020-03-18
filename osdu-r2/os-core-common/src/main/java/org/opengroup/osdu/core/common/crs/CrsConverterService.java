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

package org.opengroup.osdu.core.common.crs;

import com.google.gson.JsonSyntaxException;
import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.crs.*;
import org.opengroup.osdu.core.common.http.HttpRequest;
import org.opengroup.osdu.core.common.http.HttpResponse;
import org.opengroup.osdu.core.common.http.IHttpClient;

public class CrsConverterService implements ICrsConverterService {
    private final String rootUrl;
    private final IHttpClient httpClient;
    private final DpsHeaders headers;

    CrsConverterService(CrsConverterAPIConfig config,
                        IHttpClient httpClient,
                        DpsHeaders headers) {
        this.rootUrl = config.getRootUrl();
        this.httpClient = httpClient;
        this.headers = headers;
        if (config.apiKey != null) {
            headers.put("AppKey", config.apiKey);
        }
    }

    @Override
    public ConvertPointsResponse convertPoints(ConvertPointsRequest request) throws CrsConverterException {
        String url = this.createUrl("/convert");
        HttpResponse result = this.httpClient.send(HttpRequest.post(request).url(url).headers(this.headers.getHeaders()).build());
        return this.getResult(result, ConvertPointsResponse.class);
    }

    @Override
    public ConvertTrajectoryResponse convertTrajectory(ConvertTrajectoryRequest request) throws CrsConverterException {
        String url = this.createUrl("/convertTrajectory");
        HttpResponse result = this.httpClient.send(HttpRequest.post(request).url(url).headers(this.headers.getHeaders()).build());
        return this.getResult(result, ConvertTrajectoryResponse.class);
    }

    private CrsConverterException generateException(HttpResponse result) {
        return new CrsConverterException(
                "Error making request to CrsConverter service. Check the inner HttpResponse for more info.", result);
    }

    private String createUrl(String pathAndQuery) {
        return StringUtils.join(this.rootUrl, pathAndQuery);
    }

    private <T> T getResult(HttpResponse result, Class<T> type) throws CrsConverterException {
        if (result.isSuccessCode()) {
            try {
                return result.parseBody(type);
            } catch (JsonSyntaxException e) {
                throw new CrsConverterException("Error parsing response. Check the inner HttpResponse for more info.",
                        result);
            }
        } else {
            throw this.generateException(result);
        }
    }
}
