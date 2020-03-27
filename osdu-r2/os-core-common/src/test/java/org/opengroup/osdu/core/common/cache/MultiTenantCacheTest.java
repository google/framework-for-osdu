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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class MultiTenantCacheTest {

    @Test
    public void should_returnDifferentCaches_forDifferentTenants() {
        String t1 = "t1";
        String t2 = "t2";

        MultiTenantCache sut = new MultiTenantCache<>(new VmCache<String, String>(0, 0));

        assertEquals(sut.get(t1), sut.get(t1));
        assertEquals(sut.get(t2), sut.get(t2));
        assertNotEquals(sut.get(t1), sut.get(t2));
    }
}
