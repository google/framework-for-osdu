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

package org.opengroup.osdu.core.common.model.crs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.model.http.AppException;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversionRecord {
    private JsonObject recordJsonObject;
    private ConvertStatus convertStatus;
    @Builder.Default
    private List<String> conversionMessages = new ArrayList<>();

    public String getRecordId() {
        if (this.recordJsonObject == null) {
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "error", "record does not exist");
        }        
        JsonElement recordId = this.recordJsonObject.get("id");
        if (recordId == null || recordId.getAsString().isEmpty()) {
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "error", "record does not have id");
        }
        return recordId.getAsString();
    }    
}

