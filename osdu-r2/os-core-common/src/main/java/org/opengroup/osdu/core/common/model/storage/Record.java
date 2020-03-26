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
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.opengroup.osdu.core.common.model.entitlements.Acl;
import org.opengroup.osdu.core.common.model.legal.Legal;
import org.opengroup.osdu.core.common.model.entitlements.validation.ValidAcl;
import org.opengroup.osdu.core.common.model.storage.validation.ValidKind;
import org.opengroup.osdu.core.common.model.legal.validation.ValidLegal;
import org.opengroup.osdu.core.common.model.storage.validation.ValidationDoc;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ValidLegal
public class Record {

	private static final String DATALAKE_RECORD_PREFIX = "doc";

	@Pattern(regexp = ValidationDoc.RECORD_ID_REGEX, message = ValidationDoc.INVALID_RECORD_ID)
	@ApiModelProperty(value = SwaggerDoc.RECORD_ID_DESCRIPTION,
			required = true,
			example = SwaggerDoc.RECORD_ID_EXAMPLE)
	private String id;

	private Long version;

	@ValidKind
	@ApiModelProperty(value = SwaggerDoc.SCHEMA_REQUEST_KIND,
			required = true,
			example = SwaggerDoc.RECORD_KIND_EXAMPLE)
	private String kind;

	@NotNull(message = ValidationDoc.RECORD_ACL_NOT_EMPTY)
	@ValidAcl
	private Acl acl;

	@Valid
	private Legal legal;

	@NotEmpty(message = ValidationDoc.RECORD_PAYLOAD_NOT_EMPTY)
	@JsonInclude(Include.ALWAYS)
	private Map<String, Object> data;

	private RecordAncestry ancestry;

	private Map<String, Object>[] meta;

	public void createNewRecordId(String tenant) {
		String uuid = UUID.randomUUID().toString().replace("-", "");
		String dlId = String.format("%s:%s:%s", tenant, DATALAKE_RECORD_PREFIX, uuid);
		this.setId(dlId);
	}

	public static boolean isRecordIdValid(String recordId, String tenant) {
		return recordId.split(":")[0].equalsIgnoreCase(tenant);
	}
}
