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

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import org.opengroup.osdu.core.common.logging.audit.AuditAction;
import org.opengroup.osdu.core.common.model.http.Request;
import org.opengroup.osdu.core.common.logging.audit.AuditPayload;
import org.opengroup.osdu.core.common.logging.audit.AuditStatus;

public class LogIntegrationTest {

    DefaultLogger log = new DefaultLogger();

    @Test
    public void loggingIntegerationTest() throws Exception {
        // appEngine provided environment variables.
        AuditPayload auditPayload = AuditPayload.builder()
                .action(AuditAction.CREATE)
                .status(AuditStatus.SUCCESS)
                .message("hello")
                .resources(new ArrayList<>())
                .actionId("10001")
                .user("testUser")
                .build();


        Map<String, String> labels = new HashMap<>();
        labels.put("correlation-id", "testCorrelationId");
        labels.put("X-Cloud-Trace-Context", "f8b375ea4c7da1933f8f4829246032ef;o=0");

        Request http = Request.builder().build();
        String logname = "legaltest.log";
        long time = System.currentTimeMillis();

        log.audit(logname, auditPayload, labels);
        time = System.currentTimeMillis() - time;
        System.out.println("Time spent writing logs " + time);
        time = System.currentTimeMillis();

        log.request(logname, http, labels);
        time = System.currentTimeMillis() - time;
        System.out.println("Time spent writing logs " + time);
        time = System.currentTimeMillis();

        log.info(logname, "info", labels);
        time = System.currentTimeMillis() - time;
        System.out.println("Time spent writing logs " + time);
        time = System.currentTimeMillis();

        log.warning(logname, "warning with exception", new Exception("Test error"), labels);
        time = System.currentTimeMillis() - time;
        System.out.println("Time spent writing logs " + time);
        time = System.currentTimeMillis();

        log.warning(logname, "warning no exception", labels);
        time = System.currentTimeMillis() - time;
        System.out.println("Time spent writing logs " + time);
        time = System.currentTimeMillis();

        log.error(logname, "error with exception", new Exception("Test error"), labels);
        time = System.currentTimeMillis() - time;
        System.out.println("Time spent writing logs " + time);
        time = System.currentTimeMillis();

        log.audit(logname, auditPayload, labels);
        time = System.currentTimeMillis() - time;
        System.out.println("Time spent writing logs " + time);
        time = System.currentTimeMillis();

        log.error(logname, "error no exception", labels);
        time = System.currentTimeMillis() - time;
        System.out.println("Time spent writing logs " + time);

        assertTrue("Expected time to be no more than 50 millisecond", time <= 50);
        Thread.sleep(6000);// wait for batch settings to kick in

    }
}
