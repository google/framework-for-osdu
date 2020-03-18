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

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
public class RecordStatus {

    private String id;
    private String kind;
    private String operationType;

    private IndexingStatus status;

    @ToString.Exclude private IndexProgress indexProgress;

    public String getLatestTrace() {
        if (indexProgress != null && indexProgress.getTrace() != null && indexProgress.getTrace().size() > 0) {
            return indexProgress.getTrace().peek();
        }
        return null;
    }
}
