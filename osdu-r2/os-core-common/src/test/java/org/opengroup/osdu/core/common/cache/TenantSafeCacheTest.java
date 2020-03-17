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

package org.opengroup.osdu.core.common.cache;

import org.junit.Test;

import static org.mockito.Mockito.*;

public class TenantSafeCacheTest {

    @Test(expected = NullPointerException.class)
    public void should_throwNullError_when_givenNullTenantName() {
        new TenantSafeCache<String>(null, mock(ICache.class));
    }

    @Test
    public void should_addTenantNamToKey_when_addingToCache() {
        ICache wrapped = mock(ICache.class);
        TenantSafeCache<String> sut = new TenantSafeCache<String>("tenant1", wrapped);

        sut.put("key", "value");

        verify(wrapped, times(1)).put("tenant1key", "value");
    }

    @Test
    public void should_addTenantNamToKey_when_deletingFromCache() {
        ICache wrapped = mock(ICache.class);
        TenantSafeCache<String> sut = new TenantSafeCache<String>("tenant1", wrapped);

        sut.delete("key");

        verify(wrapped, times(1)).delete("tenant1key");
    }

    @Test
    public void should_addTenantNamToKey_when_retrievingfromCache() {
        ICache wrapped = mock(ICache.class);
        TenantSafeCache<String> sut = new TenantSafeCache<String>("tenant1", wrapped);

        sut.get("key");

        verify(wrapped, times(1)).get("tenant1key");
    }


    @Test
    public void should_callWrappedClearCache() {
        ICache wrapped = mock(ICache.class);
        TenantSafeCache<String> sut = new TenantSafeCache<String>("tenant1", wrapped);

        sut.clearAll();

        verify(wrapped, times(1)).clearAll();
    }
}
