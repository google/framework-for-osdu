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

import org.junit.Assert;
import org.junit.Test;
import org.opengroup.osdu.core.common.model.http.HeadersToLog;

import java.util.*;

public class HeadersToLogTests {

    @Test
    public void should_returnMapOfVars_when_givenMapWithMatchingKeys(){
        Map<String, String> map = new HashMap<>();
        map.put("1", "context");
        map.put("x-cloud-trace-context", "context");
        map.put("correlation-id", "corid");
        map.put("account-id", "accountid");
        map.put("x-cloud-trace-context25", "context25");

        Map<String, String>  result = new HeadersToLog(Collections.singletonList("x-cloud-trace-context"))
                .createStandardLabelsFromMap(map);

        Assert.assertEquals(3, result.size());
        Assert.assertEquals("corid", result.get(HeadersToLog.COR_ID));
        Assert.assertEquals("context", result.get("x-cloud-trace-context"));
        Assert.assertEquals("accountid", result.get(HeadersToLog.ACCOUNT_ID));
    }
    @Test
    public void should_returnMapOfVars_when_ignoringCase(){
        Map<String, String> map = new HashMap<>();
        map.put("X-cloud-trace-context", "context");
        map.put("coRRelation-id", "corid");
        map.put("account-ID", "accountid");
        map.put("data-partition-ID", "partition");

        Map<String, String>  result = new HeadersToLog(Collections.singletonList("x-cloud-trace-context")).
                createStandardLabelsFromMap(map);

        Assert.assertEquals(4, result.size());
        Assert.assertEquals("corid", result.get(HeadersToLog.COR_ID));
        Assert.assertEquals("accountid", result.get(HeadersToLog.ACCOUNT_ID));
        Assert.assertEquals("partition", result.get(HeadersToLog.DATA_PARTITION_ID));
    }
    @Test
    public void should_returnEmptyMap_when_givenNullMap(){
        Map<String, String>  result = new HeadersToLog(Collections.emptyList())
                .createStandardLabelsFromMap(null);
        Assert.assertEquals(0, result.size());
    }
}
