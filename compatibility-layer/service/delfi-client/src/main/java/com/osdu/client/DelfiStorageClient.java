/*
 * Copyright 2019 Google LLC
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

package com.osdu.client;

import com.osdu.model.Record;
import com.osdu.model.delfi.DelfiRecord;
import com.osdu.model.delfi.SaveRecordsResult;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(url = "${osdu.delfi.portal.url}/de/storage/v2/records", name = "delfi.storage.client")
public interface DelfiStorageClient {

  @GetMapping("/{recordId}")
  DelfiRecord getRecord(@PathVariable("recordId") String recordId,
      @RequestHeader("Authorization") String authorizationToken,
      @RequestHeader("slb-data-partition-id") String partition,
      @RequestHeader("AppKey") String applicationKey);

  @PutMapping
  SaveRecordsResult putRecords(@RequestBody List<Record> records,
      @RequestHeader("Authorization") String authorizationToken,
      @RequestHeader("slb-data-partition-id") String partition,
      @RequestHeader("AppKey") String applicationKey);
}