// Copyright 2017-2019, Schlumberger
// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.core.common.model.storage;

import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Builder;
import org.opengroup.osdu.core.common.model.storage.validation.ValidKind;
import org.opengroup.osdu.core.common.model.storage.validation.ValidNotNullArray;
import org.opengroup.osdu.core.common.model.storage.validation.ValidationDoc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.annotations.ApiModelProperty;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Schema {

	@ValidKind
	@NotNull
	@ApiModelProperty(value = SwaggerDoc.SCHEMA_REQUEST_KIND,
			required = true,
			example = SwaggerDoc.RECORD_KIND_EXAMPLE)
	private String kind;

	@Valid
	@NotEmpty(message = ValidationDoc.SCHEMA_ITEMS_NOT_EMPTY)
	@ValidNotNullArray
	private SchemaItem[] schema;

	private Map<String, Object> ext;

}
