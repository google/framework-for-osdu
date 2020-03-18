// Copyright 2017-2019, Schlumberger
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

package org.opengroup.osdu.core.common.http;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
@PrepareForTest({JWT.class})
public class HeadersUtilTest {

    @Mock
    private Claim emailClaim;

    @Mock
    private DecodedJWT jwt;

    @Before
    public void setup() {
        initMocks(this);

        when(jwt.getClaim("email")).thenReturn(emailClaim);
    }

    @Test
    public void should_return_header_logs_when_header_contains_email() {
        List<String> token = new ArrayList<>();
        token.add("any token");
        List<String> onBehalf = new ArrayList<>();
        onBehalf.add("bearer invalidJwtToken");
        List<String> correlationId = new ArrayList<>();
        correlationId.add("any correlationId");
        List<String> userEmail = new ArrayList<>();
        userEmail.add("abc@xyz.com");
        List<String> accountId = new ArrayList<>();
        accountId.add("any account");

        MultiValueMap<String, String> requestHeaders = new LinkedMultiValueMap<>();;
        requestHeaders.put(DpsHeaders.AUTHORIZATION, token);
        requestHeaders.put(DpsHeaders.ON_BEHALF_OF, onBehalf);
        requestHeaders.put(DpsHeaders.CORRELATION_ID, correlationId);
        requestHeaders.put(DpsHeaders.USER_EMAIL, userEmail);
        requestHeaders.put(DpsHeaders.ACCOUNT_ID, accountId);

        DpsHeaders headers = DpsHeaders.createFromEntrySet(requestHeaders.entrySet());

        assertEquals("account id: any account | on behalf: bearer invalidJwtToken | user email: any onBehalf | correlation id: " +
                "any correlationId", HeadersUtil.toLogMsg(headers, "any onBehalf"));
    }
}