/*
 * Copyright 2020 Google LLC
 * Copyright 2017-2019, Schlumberger
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

package org.opengroup.osdu.core.common.model.http;

import com.google.common.base.Strings;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;

public class DpsHeaders {
    public static final String ACCOUNT_ID = "account-id";
    public static final String ON_BEHALF_OF = "on-behalf-of";
    public static final String CORRELATION_ID = "correlation-id";
    public static final String DATA_PARTITION_ID = "data-partition-id";
    public static final String USER_EMAIL = "user";
    public static final String AUTHORIZATION = "authorization";
    public static final String CONTENT_TYPE = "content-type";
    public static final String LEGAL_TAGS = "legal-tags";
    public static final String ACL_HEADER = "acl";

    public static final String PRIMARY_PARTITION_ID = "primary-account-id";
    public static final String FRAME_OF_REFERENCE = "frame-of-reference";

    private static final HashSet<String> headerKeys = new HashSet<>();

    static {
        headerKeys.add(ACCOUNT_ID);
        headerKeys.add(DATA_PARTITION_ID);
        headerKeys.add(ON_BEHALF_OF);
        headerKeys.add(CORRELATION_ID);
        headerKeys.add(AUTHORIZATION);
        headerKeys.add(USER_EMAIL);
        headerKeys.add(CONTENT_TYPE);
        headerKeys.add(LEGAL_TAGS);
        headerKeys.add(ACL_HEADER);
    }

    private final Map<String, String> headers = new HashMap<>();

    public DpsHeaders() {
        this.headers.put(CONTENT_TYPE, "application/json");
    }

    public static DpsHeaders createFromEntrySet(Set<Map.Entry<String, List<String>>> input) {
        DpsHeaders output = new DpsHeaders();
        input.forEach(entry -> {
            String key = entry.getKey().toLowerCase();
            if (headerKeys.contains(key)) {
                output.headers.put(key, StringUtils.join(entry.getValue(), ','));
            }
        });
        return output;
    }

    public static DpsHeaders createFromMap(Map<String, String> input) {
        DpsHeaders output = new DpsHeaders();
        output.addFromMap(input);
        return output;
    }

    protected void addFromMap(Map<String, String> input)
    {
        input.forEach((k, v) -> {
            String key = k.toLowerCase();
            if (headerKeys.contains(key) || k.toLowerCase().startsWith("x-")) {
                this.headers.put(key, v);
            }
        });
    }

    public Map<String, String> getHeaders() {
        return this.headers;
    }

    public String getAccountId() {
        return this.getHeader(ACCOUNT_ID);
    }

    public String getPartitionId() {
        return this.getHeader(DATA_PARTITION_ID);
    }

    public String getPartitionIdWithFallbackToAccountId() {
        String output = getPartitionId();
        if (Strings.isNullOrEmpty(output))
            output = getAccountId();
        return output;
    }

    public String getOnBehalfOf() {
        return this.getHeader(ON_BEHALF_OF);
    }

    public String getCorrelationId() {
        return this.getHeader(CORRELATION_ID);
    }

    public String getUserEmail() {
        return this.getHeader(USER_EMAIL);
    }

    public String getLegalTags() {
        return this.getHeader(LEGAL_TAGS);
    }

    public String getAcl() {
        return this.getHeader(ACL_HEADER);
    }

    public String getAuthorization() {
        return this.getHeader(AUTHORIZATION);
    }

    public String getContentType() {
        return this.getHeader(CONTENT_TYPE);
    }

    public void put(String key, String value) {
        this.headers.put(key, value);
    }

    public void addCorrelationIdIfMissing() {
        if (StringUtils.isBlank(this.getCorrelationId())) {
            this.headers.put(CORRELATION_ID, UUID.randomUUID().toString());
        }
    }

    private String getHeader(String key) {
      return this.headers.get(key.toLowerCase());
    }
}
