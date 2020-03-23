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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResponseHeaders {
    public static final Map<String, List<Object>> STANDARD_RESPONSE_HEADERS = new HashMap<>();

    static {
        STANDARD_RESPONSE_HEADERS.put("Access-Control-Allow-Origin", new ArrayList<Object>() {
            {
                add("*");
            }
        });
        STANDARD_RESPONSE_HEADERS.put("Access-Control-Allow-Headers", new ArrayList<Object>() {
            {
                add("origin, content-type, accept, authorization, account-id, data-partition-id, correlation-id, on-behalf-of, appkey");
            }
        });
        STANDARD_RESPONSE_HEADERS.put("Access-Control-Allow-Methods", new ArrayList<Object>() {
            {
                add("GET, POST, PUT, DELETE, OPTIONS, HEAD");
            }
        });
        STANDARD_RESPONSE_HEADERS.put("Access-Control-Allow-Credentials", new ArrayList<Object>() {
            {
                add("true");
            }
        });
        STANDARD_RESPONSE_HEADERS.put("X-Frame-Options", new ArrayList<Object>() {
            {
                add("DENY");
            }
        });
        STANDARD_RESPONSE_HEADERS.put("X-XSS-Protection", new ArrayList<Object>() {
            {
                add("1; mode=block");
            }
        });
        STANDARD_RESPONSE_HEADERS.put("X-Content-Type-Options", new ArrayList<Object>() {
            {
                add("nosniff");
            }
        });
        STANDARD_RESPONSE_HEADERS.put("Cache-Control", new ArrayList<Object>() {
            {
                add("no-cache, no-store, must-revalidate");
            }
        });
        STANDARD_RESPONSE_HEADERS.put("Content-Security-Policy", new ArrayList<Object>() {
            {
                add("default-src 'self'");
            }
        });
        STANDARD_RESPONSE_HEADERS.put("Strict-Transport-Security", new ArrayList<Object>() {
            {
                add("max-age=31536000; includeSubDomains");
            }
        });
        STANDARD_RESPONSE_HEADERS.put("Expires", new ArrayList<Object>() {
            {
                add("0");
            }
        });
    }
}