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


import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.springframework.web.context.annotation.RequestScope;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RequestScope
public class RecordChangedMessages {

    private String messageId;
    private String publishTime;
    private String data;
    private Map<String, String> attributes;

    public String getDataPartitionId() {
        String output = attributes.get(DpsHeaders.DATA_PARTITION_ID);
        if(Strings.isNullOrEmpty(output))
            output =  attributes.get(DpsHeaders.ACCOUNT_ID);
        return output;
    }

    public boolean missingAccountId() {
        return this.attributes == null || Strings.isNullOrEmpty(this.getDataPartitionId());
    }

    public String getCorrelationId() {
        return attributes.get(DpsHeaders.CORRELATION_ID.toLowerCase());
    }

    public boolean hasCorrelationId() {
        return this.attributes != null && !Strings.isNullOrEmpty(this.getCorrelationId());
    }
}
