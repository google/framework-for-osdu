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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonRawValue;

import lombok.Data;

@Data
public class MultiRecordResponse {
	/**
	 * The full record for each id requested.
	 * If a conversion is requested and a field has an error during conversion, it won't show up here but with error message in convertStatus.
	 */
	@JsonRawValue
	private List<String> records;
	/**
	 * Any Reocrd Ids that were requested but not found in Storage.
	 */
	private List<String> notFound;
	/**
	 * A map of record ids to the conversion status.
	 * Scenario 1: Empty. If no conversion is requested.
	 * Scenario 2: All the errors during conversion or 'Success' indicating no conversion error.
	 *              RecordId: [Error1, Error2,...] or RecordId: ['Success']
	 * Scenario 3: 'No FoR' if no frame of reference was provided for the Record.
	 */
	private List<ConversionStatus> conversionStatuses;
}
