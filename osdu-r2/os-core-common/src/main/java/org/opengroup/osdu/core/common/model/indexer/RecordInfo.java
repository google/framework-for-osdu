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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.model.http.AppException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordInfo {

    private static final long serialVersionUID = 1L;

    private String id;
    private String kind;
    private String op;

    public static Map<String, Map<String, OperationType>> getUpsertRecordIds(List<RecordInfo> msgs) throws AppException {

        Map<String, Map<String, OperationType>> kindRecordOpMap = new HashMap<>();

        try {
            for (RecordInfo msg : msgs) {
                OperationType op = OperationType.valueOf(msg.getOp());
                if (op == OperationType.create || op == OperationType.update) {
                    Map<String, OperationType> idOperationMap = kindRecordOpMap.containsKey(msg.getKind()) ? kindRecordOpMap.get(msg.getKind()) : new HashMap<>();
                    idOperationMap.put(msg.getId(), OperationType.valueOf(msg.getOp()));
                    kindRecordOpMap.put(msg.getKind(), idOperationMap);
                }
            }
        } catch (Exception e) {
            throw new AppException(HttpStatus.SC_BAD_REQUEST, "Request parsing error", "Error parsing upsert records in request payload.", e);
        }
        return kindRecordOpMap;
    }

    public static Map<String, List<String>> getDeleteRecordIds(List<RecordInfo> msgs) {

        Map<String, List<String>> deleteRecordMap = new HashMap<>();

        try {
            for (RecordInfo msg : msgs) {
                OperationType op = OperationType.valueOf(msg.getOp());
                if (op == OperationType.purge || op == OperationType.delete) {
                    String kind = msg.getKind();
                    if (!deleteRecordMap.containsKey(kind)) {
                        deleteRecordMap.put(kind, new ArrayList<>());
                    }
                    deleteRecordMap.get(kind).add(msg.getId());
                }
            }
        } catch (Exception e) {
            throw new AppException(HttpStatus.SC_BAD_REQUEST, "Request parsing error", "Error parsing delete records in request payload.", e);
        }
        return deleteRecordMap;
    }

    public static Map<String, OperationType> getSchemaMsgs(List<RecordInfo> msgs) {

        Map<String, OperationType> schemaOperations = new HashMap<>();

        try {
            for (RecordInfo msg : msgs) {
                OperationType op = OperationType.valueOf(msg.getOp());
                if (op == OperationType.create_schema || op == OperationType.purge_schema) {
                    schemaOperations.put(msg.getKind(), op);
                }
            }
        } catch (Exception e) {
            throw new AppException(HttpStatus.SC_BAD_REQUEST, "Request parsing error", "Error parsing schema updates in request payload.", e);
        }
        return schemaOperations;
    }
}
