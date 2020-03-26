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

package org.opengroup.osdu.core.common.search;

import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.opengroup.osdu.core.common.model.search.IndexInfo;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface IndicesService {

    boolean createIndex(RestHighLevelClient client, String index, Settings settings, String type, Map<String, Object> mapping) throws ElasticsearchStatusException, IOException;

    boolean isIndexExist(RestHighLevelClient client, String index) throws IOException;

    boolean deleteIndex(RestHighLevelClient client, String index) throws Exception;

    boolean deleteIndex(String index) throws Exception;

    List<IndexInfo> getIndexInfo(RestHighLevelClient client, String indexPattern) throws IOException;
}
