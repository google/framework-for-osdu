/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.file.config;

import org.hibernate.validator.cfg.GenericConstraintDef;
import org.hibernate.validator.spi.cfg.ConstraintMappingContributor;
import org.opengroup.osdu.core.common.model.file.FileListRequest;
import org.opengroup.osdu.core.common.model.file.FileLocationRequest;
import org.opengroup.osdu.core.common.model.file.LocationRequest;
import org.opengroup.osdu.file.validation.annotation.ValidFileListRequest;
import org.opengroup.osdu.file.validation.annotation.ValidFileLocationRequest;
import org.opengroup.osdu.file.validation.annotation.ValidLocationRequest;
import org.opengroup.osdu.file.validation.group.Extended;
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
        .type(LocationRequest.class)
        .constraint(new GenericConstraintDef<>(ValidLocationRequest.class)
            .groups(Extended.class));

    builder.addConstraintMapping()
        .type(FileLocationRequest.class)
        .constraint(new GenericConstraintDef<>(ValidFileLocationRequest.class)
            .groups(Extended.class));

    builder.addConstraintMapping()
        .type(FileListRequest.class)
        .constraint(new GenericConstraintDef<>(ValidFileListRequest.class)
            .groups(Extended.class));
  }
}
