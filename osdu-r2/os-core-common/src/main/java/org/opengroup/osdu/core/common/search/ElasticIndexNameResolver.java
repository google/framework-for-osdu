/*
 * Copyright 2020 Google LLC
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

package org.opengroup.osdu.core.common.search;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 * get elastic index name for the kind and cache the mapping kind <-> index-name
 *
 * elastic index name has following restrictions
 *  - must not contain the #, \, /, *, ?, ", <, >, |, :
 *  - must not start with _, - or +
 *  - must not be . or ..
 *  - must be lowercase
 *  restriction can be found here:
 *  https://github.com/elastic/elasticsearch/blob/870a913217be6de41c4f0a91c9fa5493017c8554/server/src/test/java/org/elasticsearch/cluster/metadata/MetaDataCreateIndexServiceTests.java#L400
 * */
@Component
public class ElasticIndexNameResolver {

    private final Map<String, String> KIND_INDEX_MAP = new ConcurrentHashMap();
    private final Map<String, String> INDEX_KIND_MAP = new ConcurrentHashMap();

    public String getIndexNameFromKind(String kind) {

        String index = kind.replace(":", "-").toLowerCase();
        if (KIND_INDEX_MAP.containsKey(kind)) {
            return KIND_INDEX_MAP.get(kind);
        }
        if (!KIND_INDEX_MAP.containsKey(kind)) {
            KIND_INDEX_MAP.putIfAbsent(kind, index);
            INDEX_KIND_MAP.putIfAbsent(index, kind);
        }

        return KIND_INDEX_MAP.get(kind);
    }

    public String getKindFromIndexName(String indexName) {

        if (INDEX_KIND_MAP.containsKey(indexName)) {
            return INDEX_KIND_MAP.get(indexName);
        }

        return indexName.replace("-", ":").toLowerCase();
    }
}
