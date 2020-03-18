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

import com.google.gson.Gson;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Builder
@Data
public class HttpRequest {
    public static final String PATCH = "PATCH";
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String GET = "GET";
    public static final String DELETE = "DELETE";

    String httpMethod;
    String url;
    String body;

    @Builder.Default
    Map<String, String> headers = new HashMap<>();
    @Builder.Default
    int connectionTimeout = 5000;
    @Builder.Default
    boolean followRedirects = true;

    public static <T> HttpRequestBuilder post(T body) {
        return HttpRequest.builder().httpMethod(POST).body(new Gson().toJson(body));
    }

    public static HttpRequestBuilder post() {
        return HttpRequest.builder().httpMethod(POST);
    }

    public static <T> HttpRequestBuilder put(T body) {
        return HttpRequest.builder().httpMethod(PUT).body(new Gson().toJson(body));
    }

    public static HttpRequestBuilder put() {
        return HttpRequest.builder().httpMethod(PUT);
    }

    public static HttpRequestBuilder get() {
        return HttpRequest.builder().httpMethod(GET);
    }

    public static HttpRequestBuilder delete() {
        return HttpRequest.builder().httpMethod(DELETE);
    }

    @Override
    public String toString() {
        return String.format("%s, httpMethod=%s", url, httpMethod);
    }
}
