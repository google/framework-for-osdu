/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.workflow.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengroup.osdu.core.common.model.DataType;
import org.opengroup.osdu.core.common.model.WorkflowType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngestionStrategy {

  @JsonProperty(Fields.WORKFLOW_TYPE)
  WorkflowType workflowType;

  @JsonProperty(Fields.DATA_TYPE)
  DataType dataType;

  @JsonProperty(Fields.USER_ID)
  String userId;

  @JsonProperty(Fields.DAG_NAME)
  String dagName;

  public static final class Fields {

    public static final String WORKFLOW_TYPE = "WorkflowType";
    public static final String DATA_TYPE = "DataType";
    public static final String USER_ID = "UserID";
    public static final String DAG_NAME = "DAGName";

    private Fields() {
    }
  }

}
