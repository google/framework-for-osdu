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

package org.opengroup.osdu.workflow.config;

import org.hibernate.validator.cfg.GenericConstraintDef;
import org.hibernate.validator.spi.cfg.ConstraintMappingContributor;
import org.opengroup.osdu.core.common.model.workflow.StartWorkflowRequest;
import org.opengroup.osdu.workflow.model.GetStatusRequest;
import org.opengroup.osdu.workflow.validation.annotation.ValidGetStatusRequest;
import org.opengroup.osdu.workflow.validation.annotation.ValidStartWorkflowRequest;
import org.opengroup.osdu.workflow.validation.group.Extended;
import org.springframework.stereotype.Component;

@Component
public class RequestConstraintMappingContributor implements ConstraintMappingContributor {

  /**
   * Programmatic constraint definition and declaration for the requests.
   *
   * @param builder constraint mapping builder
   */
  public void createConstraintMappings(ConstraintMappingBuilder builder) {
    builder.addConstraintMapping()
        .type(GetStatusRequest.class)
        .constraint(new GenericConstraintDef<>(ValidGetStatusRequest.class)
            .groups(Extended.class));

    builder.addConstraintMapping()
        .type(StartWorkflowRequest.class)
        .constraint(new GenericConstraintDef<>(ValidStartWorkflowRequest.class)
            .groups(Extended.class));

  }
}
