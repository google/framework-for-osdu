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

package org.opengroup.osdu.core.common.multitenancy;

import org.opengroup.osdu.core.common.model.http.AppException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
@Primary
public class TenantInfoFactory implements FactoryBean<TenantInfo> {

	@Autowired
	private ITenantFactory tenantFactory;

	@Autowired
	private DpsHeaders headers;

	@Override
	public TenantInfo getObject() throws Exception {
		String id = this.headers.getPartitionIdWithFallbackToAccountId();
		TenantInfo tenantInfo = this.tenantFactory.getTenantInfo(id);
		if (tenantInfo == null) {
			throw AppException.createUnauthorized(String.format("could not retrieve tenant info for data partition id: %s", id));
		}
		return tenantInfo;
	}

	@Override
	public Class<?> getObjectType() {
		return TenantInfo.class;
	}

	@Override
	public boolean isSingleton() {
		return false;
	}
}
