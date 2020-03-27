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

package org.opengroup.osdu.core.common.model.http;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@Builder
public class AppError implements Serializable {
    private static final long serialVersionUID = 2405172041950241677L;
    private int code;
    private String reason;
    private String message;
    @JsonIgnore
    private String[] errors;
    // exclude debuggingInfo & originalException properties in response deserialization as they are not
    // required for swagger endpoint and Portal send weird multipart Content-Type in request
    @JsonIgnore
    private String debuggingInfo;
    @JsonIgnore
    private Exception originalException;

    //AppException creates App Errors with only these 3 attributes
    public AppError(int code, String reason, String message){
        this.code = code;
        this.reason = reason;
        this.message = message;
    }
}

