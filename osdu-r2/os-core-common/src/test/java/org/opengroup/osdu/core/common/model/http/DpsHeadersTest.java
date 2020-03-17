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

package org.opengroup.osdu.core.common.model.http;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DpsHeadersTest {

    @Test
    public void should_returnTenant_from_givenMap() {
        DpsHeaders sut = DpsHeaders.createFromMap(this.getMap());
        assertEquals("common", sut.getAccountId());
    }

    @Test
    public void should_returnDataPartition_from_givenMap() {
        DpsHeaders sut = DpsHeaders.createFromMap(this.getMap());
        assertEquals("partitionId", sut.getPartitionId());
    }

    @Test
    public void should_returnDataPartition_when_isProvided_butAccountId_whenNot() {
        DpsHeaders sut = DpsHeaders.createFromMap(this.getMap());
        assertEquals("partitionId", sut.getPartitionIdWithFallbackToAccountId());

        Map<String, String> map = this.getMap();
        map.remove(DpsHeaders.DATA_PARTITION_ID);
        sut = DpsHeaders.createFromMap(map);
        assertEquals("common", sut.getPartitionIdWithFallbackToAccountId());
    }

    @Test
    public void should_returnAuth_from_givenMap() {
        DpsHeaders sut = DpsHeaders.createFromMap(this.getMap());
        assertEquals("123456", sut.getAuthorization());
    }

    @Test
    public void should_returnPrincipal_from_givenMap() {
        DpsHeaders sut = DpsHeaders.createFromMap(this.getMap());
        assertEquals("abc@xyz.com", sut.getOnBehalfOf());
    }

    @Test
    public void should_returnCorId_from_givenMap() {
        DpsHeaders sut = DpsHeaders.createFromMap(this.getMap());
        assertEquals("corId", sut.getCorrelationId());
    }

    @Test
    public void should_returnCorId_when_givenLowerCaseHeader() {
        Map<String, String> map = new HashMap<>();
        map.put(DpsHeaders.CORRELATION_ID.toLowerCase(), "corId");
        DpsHeaders sut = DpsHeaders.createFromMap(map);
        assertEquals("corId", sut.getCorrelationId());
    }

    @Test
    public void should_returnAuthToken_from_givenMultivaliedMap() {
        DpsHeaders sut = DpsHeaders.createFromEntrySet(this.getMultiMap());
        assertEquals("123456", sut.getAuthorization());
    }

    @Test
    public void should_returnContentType_from_givenMultivaliedMap() {
        DpsHeaders sut = DpsHeaders.createFromEntrySet(this.getMultiMap());
        assertEquals("application/json", sut.getContentType());
    }

    @Test
    public void should_generateCorID_when_doesntHaveCorrelationId() {
        Map<String, String> map = this.getMap();
        map.put(DpsHeaders.CORRELATION_ID, "");
        DpsHeaders sut = DpsHeaders.createFromMap(map);
        assertTrue(StringUtils.isBlank(sut.getCorrelationId()));

        sut.addCorrelationIdIfMissing();

        assertFalse(StringUtils.isBlank(sut.getCorrelationId()));

    }

    @Test
    public void should_addHeaderValue_when_givenKeyValuePair() {
        DpsHeaders sut = DpsHeaders.createFromEntrySet(this.getMultiMap());
        assertNull(sut.getUserEmail());

        sut.put(DpsHeaders.USER_EMAIL, "abc@xyz.com");

        assertEquals("abc@xyz.com", sut.getUserEmail());
    }

    @Test
    public void should_returnLegalTags_from_givenMap() {
        DpsHeaders sut = DpsHeaders.createFromMap(this.getMap());
        assertEquals("{\"legal\":{\"legaltags\":[\"public-usa-dataset-1\"],"
            + "\"otherRelevantDataCountries\":[\"US\"]}}",
            sut.getLegalTags());
    }

    @Test
    public void should_returnAclHeader_from_givenMap() {
        DpsHeaders sut = DpsHeaders.createFromMap(this.getMap());
        assertEquals("{\"acl\":{\"owners\":[\"data.default.owners@test.com\"],"
            + "\"viewers\":[\"data.default.viewers@test.com\"]}}",
            sut.getAcl());
    }

    public Map<String, String> getMap() {
        Map<String, String> map = new HashMap<>();
        map.put(DpsHeaders.ON_BEHALF_OF, "abc@xyz.com");
        map.put(DpsHeaders.AUTHORIZATION, "123456");
        map.put(DpsHeaders.ACCOUNT_ID, "common");
        map.put(DpsHeaders.CORRELATION_ID, "corId");
        map.put(DpsHeaders.DATA_PARTITION_ID, "partitionId");
        map.put(DpsHeaders.LEGAL_TAGS, "{\"legal\":{\"legaltags\":[\"public-usa-dataset-1\"],\"otherRelevantDataCountries\":[\"US\"]}}");
        map.put(DpsHeaders.ACL_HEADER, "{\"acl\":{\"owners\":[\"data.default.owners@test.com\"],\"viewers\":[\"data.default.viewers@test.com\"]}}");
        return map;
    }

    public Set<Map.Entry<String, List<String>>> getMultiMap() {
        Set<Map.Entry<String, List<String>>> map = new HashSet<>();
        map.add(new Map.Entry<String, List<String>>() {
            @Override
            public String getKey() {
                return DpsHeaders.AUTHORIZATION;
            }

            @Override
            public List<String> getValue() {
                return new ArrayList<String>() {
                    private static final long serialVersionUID = -5535083774657557076L;

                    {
                        this.add("123456");
                    }
                };
            }

            @Override
            public List<String> setValue(List<String> value) {
                return null;
            }
        });
        map.add(new Map.Entry<String, List<String>>() {
            @Override
            public String getKey() {
                return DpsHeaders.ACCOUNT_ID;
            }

            @Override
            public List<String> getValue() {
                return new ArrayList<String>() {
                    private static final long serialVersionUID = -5754729534605433697L;

                    {
                        this.add("common");
                    }
                };
            }

            @Override
            public List<String> setValue(List<String> value) {
                return null;
            }
        });
        return map;
    }
}
