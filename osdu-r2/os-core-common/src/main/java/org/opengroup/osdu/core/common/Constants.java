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

package org.opengroup.osdu.core.common;

public final class Constants {

    // Indexer parameters
    public static final String REINDEX_RELATIVE_URL = "/api/indexer/v2/_dps/task-handlers/reindex-worker";
    public static final String WORKER_RELATIVE_URL = "/api/indexer/v2/_dps/task-handlers/index-worker";

    public static final String INDEXER_QUEUE_IDENTIFIER = "indexer-queue-osdu";

    // Search parameters
    public static final int QUERY_DEFAULT_LIMIT = 10;
    public static final int QUERY_LIMIT_MAXIMUM = 100;
    public static final int AGGREGATION_SIZE = 1000;

    public static final String PROPERTIES = "properties";
    public static final String DATA = "data";
    public static final String TYPE = "type";
    public static final String KEYWORD = "keyword";
    public static final String FIELDS = "fields";

    //headers needed to call storage and get converted data
    public static final String SLB_FRAME_OF_REFERENCE_VALUE = "units=SI;crs=wgs84;elevation=msl;azimuth=true north;dates=utc;";
    
    
}