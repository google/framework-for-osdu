// Copyright 2017-2019, Schlumberger
// Copyright 2020 Google LLC
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

import com.google.api.client.http.HttpMethods;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@RequestScope
public class UrlFetchServiceImpl implements IUrlFetchService {

    @Autowired
    private HttpClientHandler httpClientHandler;

    public HttpResponse sendRequest(String httpMethod, String address, DpsHeaders headers, Map<String, String> queryParams, String body) throws URISyntaxException {

        URIBuilder builder = new URIBuilder(address);
        if (queryParams != null && !queryParams.isEmpty()) {
            for (String param : queryParams.keySet()) {
                builder.setParameter(param, queryParams.get(param));
            }
        }

        switch (httpMethod) {
            case HttpMethods.POST: {
                HttpPost request = new HttpPost(builder.build());
                request.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
                return this.httpClientHandler.sendRequest(request, headers);
            }
            case HttpMethods.GET: {
                HttpGet request = new HttpGet(builder.build());
                return this.httpClientHandler.sendRequest(request, headers);
            }
            case HttpMethods.PUT: {
                HttpPut request = new HttpPut(builder.build());
                request.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
                return this.httpClientHandler.sendRequest(request, headers);
            }
            default:
                throw new AppException(HttpStatus.SC_NOT_FOUND, "Invalid HTTP method", "Invalid HTTP method");
        }
    }
}
