// Copyright 2017-2019, Schlumberger
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

package org.opengroup.osdu.core.common.model.legal.jobs;

import org.opengroup.osdu.core.common.model.legal.InvalidTagWithReason;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.legal.ILegalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequestScope
public class LegalTagConsistencyValidator {

	@Autowired
	private ILegalService legalService;

	@Autowired
	private JaxRsDpsLog logger;

	public LegalTagChangedCollection checkLegalTagStatusWithLegalService(LegalTagChangedCollection dto) {
		List<LegalTagChanged> statusChangedTags = dto.getStatusChangedTags();

		Set<String> requestedLegalTagNames = new HashSet<>();
		for (LegalTagChanged lt : statusChangedTags) {
			requestedLegalTagNames.add(lt.getChangedTagName());
		}

		InvalidTagWithReason[] invalidLegalTags = this.legalService.getInvalidLegalTags(requestedLegalTagNames);

		List<String> invalidLegalTagsNames = new ArrayList<>();
		for (InvalidTagWithReason legaltag : invalidLegalTags) {
			invalidLegalTagsNames.add(legaltag.getName());
		}

		for (int i = 0; i < statusChangedTags.size(); i++) {
			LegalTagChanged lt = statusChangedTags.get(i);
			if (lt.getChangedTagStatus().equalsIgnoreCase("incompliant")
					&& !invalidLegalTagsNames.contains(lt.getChangedTagName())) {
				this.logger.warning("Inconsistency between pubsub message and legal: " + lt.getChangedTagName());
				statusChangedTags.remove(lt);
			}
			if (lt.getChangedTagStatus().equalsIgnoreCase("compliant")
					&& invalidLegalTagsNames.contains(lt.getChangedTagName())) {
				this.logger.warning("Inconsistency between pubsub message and legal: " + lt.getChangedTagName());
				statusChangedTags.remove(lt);
			}
		}

		LegalTagChangedCollection validOutput = new LegalTagChangedCollection();
		validOutput.setStatusChangedTags(statusChangedTags);

		return validOutput;
	}
}