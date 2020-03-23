// Copyright 2017-2019 Schlumberger
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

// TODO: High-level file comment.

package org.opengroup.osdu.core.common.multitenancy;

import java.util.Collection;

import org.opengroup.osdu.core.common.cache.ICache;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;

public class TenantInfoMemoryRepo implements ITenantInfoRepo {

	public static final String TENANT_INFO_LIST = "Dps-Service-Utils-TenantList";

	public TenantInfoMemoryRepo(ICache<String, Collection<TenantInfo>> cache, ITenantInfoRepo wrapped) {
		this.cache = cache;
		this.wrapped = wrapped;
	}

	private final ICache<String, Collection<TenantInfo>> cache;
	private final ITenantInfoRepo wrapped;

	@Override
	public TenantInfo get(String tenantName) {
		TenantInfo ti = getTenantInfoFromCache(tenantName);
		if(ti == null) {
			this.cache.delete(TENANT_INFO_LIST);
			ti = getTenantInfoFromCache(tenantName);
		}
		return ti;
	}

	@Override
	public Collection<TenantInfo> list() {
		Collection<TenantInfo> output = this.cache.get(TENANT_INFO_LIST);

		if (output == null) {
			output = this.wrapped.list();
			if (output != null && output.size() > 0) {
				this.cache.put(TENANT_INFO_LIST, output);
			}
		}

		return output;
	}

	private TenantInfo getTenantInfoFromCache(String tenantName) {
		Collection<TenantInfo> tenantInfoList = this.list();
		for (TenantInfo tenantInfo : tenantInfoList) {
			if (tenantInfo.getCrmAccountIds().contains(tenantName)) {
				return tenantInfo;
			}
		}
		return null;
	}
}