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

import java.util.*;

public class HeadersToLog {

    public static final String COR_ID = "correlation-id";
    public static final String ACCOUNT_ID = "account-id";
    public static final String DATA_PARTITION_ID = "data-partition-id";
    private static final HashSet<String> headerKeys = new HashSet<>();


    public HeadersToLog(List<String> headersToMap){
        headerKeys.add(ACCOUNT_ID);
        headerKeys.add(DATA_PARTITION_ID);
        headerKeys.add(COR_ID);
        for (String s : headersToMap) {
            headerKeys.add(s);
        }
    }

    public Map<String,String> createStandardLabelsFromMap(Map<String, String> input){
        Map<String,String>  output = new HashMap<>();
        if(input != null) {
            input.forEach((k, v) -> {
                String key = k.toLowerCase();
                if (headerKeys.contains(key))
                    output.put(key, v);
            });
        }
        return output;
    }
}
