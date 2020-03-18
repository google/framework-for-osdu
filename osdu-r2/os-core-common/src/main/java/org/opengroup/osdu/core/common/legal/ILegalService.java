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

package org.opengroup.osdu.core.common.legal;

import org.opengroup.osdu.core.common.model.legal.InvalidTagWithReason;
import org.opengroup.osdu.core.common.model.storage.Record;
import org.opengroup.osdu.core.common.model.storage.RecordMetadata;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ILegalService {

	void validateLegalTags(Set<String> legaltags);

	void populateLegalInfoFromParents(List<Record> inputRecords, Map<String, RecordMetadata> existingRecordsMetadata,
                                      Map<String, List<String>> recordParentMap);

	void validateOtherRelevantDataCountries(Set<String> ordc);

	InvalidTagWithReason[] getInvalidLegalTags(Set<String> legalTagNames);
}
