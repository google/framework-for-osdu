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

package org.opengroup.osdu.core.common.logging;

import org.junit.Test;
import org.opengroup.osdu.core.common.logging.audit.AuditAction;
import org.opengroup.osdu.core.common.logging.audit.AuditPayload;
import org.opengroup.osdu.core.common.logging.audit.AuditStatus;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;

public class AuditPayloadTest {

    @Test
    public void should_returnMap_when_buildIsCalled() {
        Map<String, Object> auditLogMap = new HashMap<>();
        auditLogMap.put("actionId", "10001");
        auditLogMap.put("action", AuditAction.CREATE);
        auditLogMap.put("user", "abc@slb.com");
        auditLogMap.put("status", AuditStatus.SUCCESS);
        auditLogMap.put("message", "Legal tag created");
        auditLogMap.put("resources", singletonList("resource"));

        Map expected = singletonMap("auditLog", auditLogMap);

        AuditPayload testLog = AuditPayload.builder()
                .action(AuditAction.CREATE)
                .status(AuditStatus.SUCCESS)
                .actionId("10001")
                .user("abc@slb.com")
                .message("Legal tag created")
                .resources(singletonList("resource"))
                .build();

        assertEquals(expected,testLog);
    }

    @Test (expected = NullPointerException.class)
    public void should_throwNullPointerException_when_userIsMissing() {
        AuditPayload.builder()
                .action(AuditAction.CREATE)
                .status(AuditStatus.SUCCESS)
                .actionId("10001")
                .message("Legal tag created")
                .resources(singletonList("resource"))
                .build();
    }

    @Test (expected = NullPointerException.class)
    public void should_throwNullPointerException_when_messageIsMissing() {
        AuditPayload.builder()
                .action(AuditAction.CREATE)
                .status(AuditStatus.SUCCESS)
                .actionId("10001")
                .user("testUser")
                .resources(singletonList("resource"))
                .build();
    }

    @Test (expected = NullPointerException.class)
    public void should_throwNullPointerException_when_statusIsMissing() {
        AuditPayload.builder()
                .action(AuditAction.CREATE)
                .message("Legal tag created")
                .actionId("10001")
                .user("testUser")
                .resources(singletonList("resource"))
                .build();
    }

    @Test (expected = NullPointerException.class)
    public void should_throwNullPointerException_when_actionIsMissing() {
        AuditPayload.builder()
                .message("Legal tag created")
                .status(AuditStatus.SUCCESS)
                .actionId("10001")
                .user("testUser")
                .resources(singletonList("resource"))
                .build();
    }

    @Test (expected = NullPointerException.class)
    public void should_throwNullPointerException_when_actionIdIsMissing() {
        AuditPayload.builder()
                .action(AuditAction.CREATE)
                .status(AuditStatus.SUCCESS)
                .message("Legal tag created")
                .user("testUser")
                .resources(singletonList("resource"))
                .build();
    }

    @Test (expected = NullPointerException.class)
    public void should_throwNullPointerException_when_resoucesAreMissing() {
        AuditPayload.builder()
                .action(AuditAction.CREATE)
                .status(AuditStatus.SUCCESS)
                .actionId("10001")
                .user("testUser")
                .message("Legal tag created")
                .build();
    }
}
