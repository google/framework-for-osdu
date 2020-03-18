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

package org.opengroup.osdu.core.common.logging.audit;

import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Builder
@NoArgsConstructor
public class AuditPayload extends HashMap<String, Object> {

    private List<String> resources;
    private AuditStatus status;
    private String message;
    private String actionId;
    private AuditAction action;
    private String user;

    public static class AuditPayloadBuilder {
        private AuditAction action;
        private AuditStatus status;

        public AuditPayloadBuilder action(AuditAction auditAction) {
            this.action = auditAction;
            return this;
        }

        public AuditPayloadBuilder status(AuditStatus auditStatus) {
            this.status = auditStatus;
            return this;
        }

        public AuditPayload build() {
            Preconditions.checkNotNull(this.resources, "resources must be provided");
            Preconditions.checkNotNull(this.status, "status must be provided");
            Preconditions.checkNotNull(this.message, "message must be provided");
            Preconditions.checkNotNull(this.action, "action must be provided");
            Preconditions.checkNotNull(this.actionId, "actionId must be provided");
            Preconditions.checkNotNull(this.user, "user must be provided");

            AuditPayload log = new AuditPayload();
            Map<String, Object> payload = new HashMap<>();
            payload.put("resources", this.resources);
            payload.put("status", this.status);
            payload.put("message", this.message);
            payload.put("action", this.action);
            payload.put("actionId", this.actionId);
            payload.put("user", this.user);

            log.put("auditLog", payload);
            return log;
        }
    }
}
