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

package org.opengroup.osdu.core.common.model.indexer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengroup.osdu.core.common.model.entitlements.Acl;
import org.opengroup.osdu.core.common.model.legal.Legal;
import org.opengroup.osdu.core.common.model.storage.RecordAncestry;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordIndexerPayload {

    private List<IndexSchema> schemas;
    private List<Record> records;

    @Data
    public static class Record {
        private String id;
        private String kind;
        private String namespace;
        private String type;
        private OperationType operationType;
        private long version;
        private Acl acl;
        private IndexProgress indexProgress;
        private Legal legal;
        private RecordAncestry ancestry;
        private Map<String, Object> data;
        @JsonIgnore
        private boolean schemaMissing = false;
        @JsonIgnore
        private boolean mappingMismatch = false;

        public boolean skippedDataIndexing() {
            return schemaMissing || mappingMismatch;
        }
    }
}
