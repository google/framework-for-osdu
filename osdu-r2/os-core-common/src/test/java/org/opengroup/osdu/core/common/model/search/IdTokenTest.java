// Copyright 2017-2019, Schlumberger
// Copyright 2020 Google LLC
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

package org.opengroup.osdu.core.common.model.search;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.model.search.IdToken;

@RunWith(MockitoJUnitRunner.class)
public class IdTokenTest {

    @Test
    public void should_returnTrue_givenNull_refreshTokenTest() {
        Assert.assertTrue(IdToken.refreshToken(null));
    }

    @Test
    public void should_returnTrue_givenValidToken_refreshTokenTest() {
        IdToken idToken = IdToken.builder().tokenValue("tokenValue").expirationTimeMillis(System.currentTimeMillis()).build();
        Assert.assertTrue(IdToken.refreshToken(idToken));
    }

    @Test
    public void should_returnFalse_whenTokenExpired_refreshTokenTest() {
        IdToken idToken = IdToken.builder().tokenValue("tokenValue").expirationTimeMillis(System.currentTimeMillis()).build();
        idToken.setExpirationTimeMillis(System.currentTimeMillis()+1000000L);
        idToken = IdToken.builder().tokenValue("tokenValue").expirationTimeMillis(System.currentTimeMillis()+1000000L).build();
        Assert.assertFalse(IdToken.refreshToken(idToken));
    }
}
