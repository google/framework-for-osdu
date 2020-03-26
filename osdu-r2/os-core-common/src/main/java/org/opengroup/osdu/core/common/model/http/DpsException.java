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

package org.opengroup.osdu.core.common.model.http;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.opengroup.osdu.core.common.http.HttpResponse;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class DpsException extends Exception {

    private static final long serialVersionUID = -4186527024055516452L;
    private HttpResponse httpResponse;

    public DpsException(String message, HttpResponse httpResponse) {
        super(message);
        this.httpResponse = httpResponse;
    }

    public String getCorrelationId() {
        List<String> val = this.httpResponse.getHeaders().get(DpsHeaders.CORRELATION_ID);
        if (val == null || val.size() == 0) {
            return "";
        } else {
            return String.join("", val);
        }
    }

    public String getAccountId() {
        return this.httpResponse.getRequest().getHeaders().get(DpsHeaders.ACCOUNT_ID);
    }
}
