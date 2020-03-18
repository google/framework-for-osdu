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

package org.opengroup.osdu.core.common.provider.interfaces;

import org.opengroup.osdu.core.common.cache.ICache;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;

import java.util.Collection;

public interface ITenantFactory {

	boolean exists(String tenantName);

	TenantInfo getTenantInfo(String tenantName);

	Collection<TenantInfo> listTenantInfo();

	<V> ICache<String, V> createCache(String tenantName, String host, int port, int expireTimeSeconds, Class<V> classOfV);

	void flushCache();
}