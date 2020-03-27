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

package org.opengroup.osdu.core.common.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;

public class VmCache<K, V> implements ICache<K, V> {

    private final Cache<K, V> cache;

    public VmCache(int cacheExpirationSeconds, int maximumCacheSize) {

        this.cache = CacheBuilder.newBuilder().expireAfterAccess(cacheExpirationSeconds, TimeUnit.SECONDS)
                .maximumSize(maximumCacheSize).build();
    }

    @Override
    public void put(K k, V v) {
        this.cache.put(k, v);
    }

    @Override
    public V get(K k) {
        return this.cache.getIfPresent(k);
    }

    @Override
    public void delete(K k) {
        this.cache.invalidate(k);
    }

    @Override
    public void clearAll() {
        this.cache.invalidateAll();
    }
}
