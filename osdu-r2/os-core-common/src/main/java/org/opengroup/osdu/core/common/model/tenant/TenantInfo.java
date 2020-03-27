/*
 * Copyright 2020 Google LLC
 * Copyright 2017-2019, Schlumberger
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

// TODO: High-level file comment.

package org.opengroup.osdu.core.common.model.tenant;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class TenantInfo {
	public static final String COMMON = "common";

	private Long id;
	private String name;
	private String projectId;
	private String serviceAccount;
	private String complianceRuleSet;
	private String dataPartitionId;
	private List<String> crmAccountIds;

	public static class ComplianceRuleSets {
		public static final String SHARED = "shared";
		public static final String CUSTOMER = "customer";
	}
}
