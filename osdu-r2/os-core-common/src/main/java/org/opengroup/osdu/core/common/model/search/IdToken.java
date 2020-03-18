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

package org.opengroup.osdu.core.common.model.search;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.context.annotation.RequestScope;

@Data
@Builder
@RequestScope
public class IdToken {

    private String tokenValue;
    private Long expirationTimeMillis;

    public static Boolean refreshToken(IdToken token) {

        if(token == null) return true;

        // there is no refresh token mechanism on service account id_token
        // get a new token if token has 2 minutes to expire
        long diff = token.expirationTimeMillis - System.currentTimeMillis();
        long diffMinutes = diff / (60 * 1000) % 60;

        return diffMinutes <= 2;
    }
}