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

package org.opengroup.osdu.core.common.model.http;

import lombok.Data;
import org.springframework.web.context.annotation.RequestScope;

@Data
@RequestScope
public class RequestStatus {

    /*
    * schema merge conflict
    * */
    public static final short SCHEMA_CONFLICT = 289;

    /*
     * one record-id can may end up being used with multiple records in different kinds,
     * in case if record returned for different kind, than requested one
     */
    public static final short STORAGE_CONFLICT = 296;

    /*
     * If storage service return invalidRecords:
     * storage service cannot locate the record
     */
    public static final short INVALID_RECORD = 298;

    /*
     * if call to upstream server times out
     */
    public static final short SOCKET_TIMEOUT = 509;
}
