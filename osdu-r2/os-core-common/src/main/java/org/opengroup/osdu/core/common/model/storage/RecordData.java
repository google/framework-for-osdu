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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

import static org.apache.commons.codec.binary.Base64.encodeBase64;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecordData {

	@JsonInclude(Include.ALWAYS)
	private Map<String, Object> data;

	@JsonInclude(Include.NON_NULL)
	private Map<String, Object>[] meta;

	public RecordData(Record record) {
		this.data = record.getData();
		this.meta = record.getMeta();
	}
}
