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

package org.opengroup.osdu.core.common.model.indexer;

import com.google.common.base.Strings;
import lombok.extern.java.Log;
import lombok.Data;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import javax.inject.Inject;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Log
@Data
@Component
@RequestScope
public class JobStatus {

    @Inject
    private JaxRsDpsLog jaxRsDpsLog;

    private List<RecordStatus> statusesList = new ArrayList<>();

    private List<String> debugInfos = new ArrayList<>();

    public void initialize(List<RecordInfo> recordInfos) {

        if (recordInfos == null || recordInfos.isEmpty()) return;

        List<RecordStatus> statuses = recordInfos.stream().map(msg -> RecordStatus.builder()
                .id(msg.getId())
                .kind(msg.getKind())
                .operationType(msg.getOp())
                .status(IndexingStatus.PROCESSING)
                .indexProgress(IndexProgress.builder().trace(new Stack<>()).lastUpdateTime(Instant.now().toString()).build())
                .build()).collect(Collectors.toList());

        this.statusesList.addAll(statuses);
    }

    public void addOrUpdateRecordStatus(Collection<String> ids, IndexingStatus status, int statusCode, String message, String debugInfo) {

        this.debugInfos.add(debugInfo);
        addOrUpdateRecordStatus(ids, status, statusCode, message);
    }

    public void addOrUpdateRecordStatus(String id, IndexingStatus status, int statusCode, String message, String debugInfo) {

        this.debugInfos.add(debugInfo);
        addOrUpdateRecordStatus(id, status, statusCode, message);
    }

    public void addOrUpdateRecordStatus(Collection<String> ids, IndexingStatus status, int statusCode, String message) {

        if (ids == null || ids.isEmpty()) return;
        ids.forEach(id -> addOrUpdateRecordStatus(id, status, statusCode, message));
    }

    public void addOrUpdateRecordStatus(String id, IndexingStatus status, int statusCode, String message) {
        Optional<RecordStatus> queryResult = this.statusesList.stream().filter(s -> s.getId().equalsIgnoreCase(id)).findFirst();
        if (queryResult.isPresent()) {
            RecordStatus s = queryResult.get();
            IndexProgress indexProgress = s.getIndexProgress();
            indexProgress.setStatusCode(statusCode);
            indexProgress.setLastUpdateTime(Instant.now().toString());
            if (!Strings.isNullOrEmpty(message)) {
                indexProgress.getTrace().add(message);
            }
            if (status.isWorseThan(s.getStatus())) {
                s.setStatus(status);
            }
            s.setIndexProgress(indexProgress);
        } else {
            IndexProgress indexProgress = IndexProgress.builder()
                    .trace(new Stack<>())
                    .lastUpdateTime(Instant.now().toString()).build();
            indexProgress.getTrace().add(message);
            this.statusesList.add(RecordStatus.builder().id(id).status(status).indexProgress(indexProgress).build());
        }
    }

    public List<String> getIdsByIndexingStatus(IndexingStatus indexingStatus) {

        return this.statusesList.stream().filter(s -> s.getStatus() == indexingStatus).map(RecordStatus::getId)
                .collect(Collectors.toList());
    }

    public String getRecordKindById(String id) {
        Optional<RecordStatus> optionalRecordStatus = this.statusesList.stream().filter(s -> s.getId()
                .equalsIgnoreCase(id)).findFirst();
        RecordStatus status = optionalRecordStatus.orElse(null);
        return status != null ? status.getKind() : null;
    }

    public RecordStatus getJobStatusByRecordId(String id) {
        Optional<RecordStatus> optionalRecordStatus = this.statusesList.stream().filter(s -> s.getId()
                .equalsIgnoreCase(id)).findFirst();
        return optionalRecordStatus.orElse(null);
    }

    public List<RecordStatus> getRecordStatuses(IndexingStatus indexingStatus, OperationType operationType) {
        return this.statusesList.stream().filter(
                s -> s.getStatus() == indexingStatus && s.getOperationType().equalsIgnoreCase(operationType.getValue())).collect(Collectors.toList());
    }

    /*
     * mark all the records as FAIL if for some reason they were not processed
     * */
    public void finalizeRecordStatus(String errorMessage) {
        statusesList.stream().filter(recordStatus -> recordStatus.getStatus() == IndexingStatus.PROCESSING).forEach
                (recordStatus -> {
                    recordStatus.setStatus(IndexingStatus.FAIL);
                    recordStatus.getIndexProgress().getTrace().add(errorMessage);
                });

        // dump all debug-info
        this.jaxRsDpsLog.warning(this.debugInfos);
    }
}
