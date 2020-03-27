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

package org.opengroup.osdu.core.common.http;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.common.base.Strings;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;

import java.util.ArrayList;

public class HeadersUtil {

    public static String toLogMsg(DpsHeaders headers, String userEmail) {
        ArrayList<String> msgs = new ArrayList<>();
        msgs.add(String.format("account id: %s", headers.getAccountId()));
        if(headers.getHeaders().containsKey(DpsHeaders.PRIMARY_PARTITION_ID)) {
            msgs.add(String.format("primary account id: %s", headers.getHeaders().get(DpsHeaders.PRIMARY_PARTITION_ID)));
        }
        if (!Strings.isNullOrEmpty(headers.getOnBehalfOf())) {
            String onBehalfEmail = getEmailClaim(headers.getOnBehalfOf());
            msgs.add(String.format("on behalf: %s", onBehalfEmail == null ? headers.getOnBehalfOf() : onBehalfEmail));
        }
        String email = Strings.isNullOrEmpty(userEmail) ? headers.getUserEmail() : userEmail;
        if (Strings.isNullOrEmpty(email)) {
            if (!Strings.isNullOrEmpty(headers.getAuthorization())) {
                email = getEmailClaim(headers.getAuthorization());
            }
        }
        if (!Strings.isNullOrEmpty(email)) msgs.add(String.format("user email: %s", email));
        msgs.add(String.format("correlation id: %s", headers.getCorrelationId()));
        return String.join(" | ", msgs);
    }

    public static String getEmailClaim(String bearerToken) {
        String email = null;
        try {
            String[] parts = bearerToken.trim().split(" ");
            if (parts == null || parts.length == 0) return null;
            if (parts.length == 1) {
                return parts[0];
            }
            String jwtString = parts[1];
            DecodedJWT jwt = JWT.decode(jwtString);
            if (jwt == null) return null;
            Claim emailClaim = jwt.getClaim("email");
            if (!emailClaim.isNull()) email = emailClaim.asString();
        } catch (JWTDecodeException ignored) {
        }
        return email;
    }
}
