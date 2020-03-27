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

package org.opengroup.osdu.core.common.service;

import org.junit.Test;
import org.opengroup.osdu.core.common.util.Crc32c;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class Crc32cTest {
    @Test
    public void should_returnSameHash_when_givenSameInput(){
        String input = UUID.randomUUID().toString();
        String result1 = Crc32c.hashToBase64EncodedString(input);
        String result2 = Crc32c.hashToBase64EncodedString(input);
        assertEquals(result1, result2);
    }
    @Test
    public void should_returnDifferentHash_when_givenDiffernetInput(){
        String result1 = Crc32c.hashToBase64EncodedString(UUID.randomUUID().toString());
        String result2 = Crc32c.hashToBase64EncodedString(UUID.randomUUID().toString());
        assertNotEquals(result1, result2);
    }
    @Test
    public void should_returnDiffernetHash_when_givenDifferentEncoding()throws UnsupportedEncodingException{
        String input = UUID.randomUUID().toString();
        String result1 = Crc32c.hashToBase64EncodedString(input, "UTF-16");
        String result2 = Crc32c.hashToBase64EncodedString(input, "UTF-8");
        assertNotEquals(result1, result2);
    }
    @Test(expected = UnsupportedEncodingException.class)
    public void should_throw_when_givenUnsupportedEncoding()throws UnsupportedEncodingException{
        String input = UUID.randomUUID().toString();
        Crc32c.hashToBase64EncodedString(input, "UTF-17");
    }
}
