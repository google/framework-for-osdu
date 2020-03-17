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

package org.opengroup.osdu.core.common.model.legal.jobs;

import org.opengroup.osdu.core.common.model.legal.LegalCompliance;
import org.opengroup.osdu.core.common.model.indexer.OperationType;
import org.opengroup.osdu.core.common.model.storage.RecordState;

public class ComplianceChangeInfo {

	public ComplianceChangeInfo(LegalCompliance newState, OperationType pubSubEvent, RecordState newRecordState) {
		this.newState = newState;
		this.newRecordState = newRecordState;
		this.pubSubEvent = pubSubEvent;
	}

	private LegalCompliance newState;
	private OperationType pubSubEvent;
	private RecordState newRecordState;

	public LegalCompliance getNewState() {
		return this.newState;
	}

	public OperationType getPubSubEvent() {
		return this.pubSubEvent;
	}

	public RecordState getNewRecordState() {
		return this.newRecordState;
	}
}
