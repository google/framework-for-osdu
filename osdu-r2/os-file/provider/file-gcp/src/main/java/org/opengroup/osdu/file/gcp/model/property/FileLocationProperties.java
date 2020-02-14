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

package org.opengroup.osdu.file.gcp.model.property;

import javax.validation.constraints.NotBlank;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

// TODO: remove it after defined tenant info and auth
@Getter
@ConfigurationProperties(prefix = "file.location")
@Validated
public class FileLocationProperties {

  @NotBlank
  final String bucketName;

  @NotBlank
  final String userId;

  @ConstructorBinding
  public FileLocationProperties(String bucketName, String userId) {
    this.bucketName = bucketName;
    this.userId = userId;
  }
}
