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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class VmCacheTest {
    @Test
    public void should_returnCachedItem_andThen_returnUpdateItem_andThen_notReturnDeletedItem() {

        String id = "1";
        String value = "abc";
        VmCache<String, String> sut = new VmCache<>(2, 2);

        assertNull(sut.get(id));

        sut.put(id, value);
        assertEquals(value, sut.get(id));

        sut.put(id, "newVal");
        assertEquals("newVal", sut.get(id));

        sut.delete(id);
        assertNull(sut.get(id));
    }

    @Test
    public void should_invalidateItem_after_expirationHasPassed() throws InterruptedException {
        String id = "1";
        String value = "abc";
        VmCache<String, String> sut = new VmCache<>(1, 1);

        sut.put(id, value);
        assertEquals(value, sut.get(id));
        Thread.sleep(1010);

        assertNull(sut.get(id));
    }

    @Test
    public void should_overwriteItems_after_cacheLimitIsReached() {
        String id = "1";
        String value = "abc";
        VmCache<String, String> sut = new VmCache<>(1, 1);

        sut.put(id, value);
        assertEquals(value, sut.get(id));

        sut.put("new", "value");

        assertNull(sut.get(id));
    }

    @Test
    public void should_returnCachedItem_when_itHasBeenCleared() {

        String id = "1";
        String value = "abc";
        VmCache<String, String> sut = new VmCache<>(2, 2);
        sut.put(id, value);
        assertEquals(value, sut.get(id));

        sut.clearAll();

        assertNull(sut.get(id));
    }
}
