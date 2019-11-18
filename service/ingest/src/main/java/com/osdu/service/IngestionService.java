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

package com.osdu.service;

import com.osdu.model.IngestHeaders;
import com.osdu.model.Record;
import com.osdu.model.delfi.IngestedFile;
import com.osdu.model.delfi.RequestMeta;
import com.osdu.model.delfi.signed.SignedFile;
import com.osdu.model.delfi.signed.SignedUrlResult;
import com.osdu.model.type.manifest.ManifestFile;
import com.osdu.model.type.wp.WorkProductComponent;
import java.net.URL;
import java.util.List;

public interface IngestionService {

  Record createRecordForWorkProductComponent(WorkProductComponent wpc, String wpcSrn,
      List<String> srns, RequestMeta requestMeta, IngestHeaders headers);

  SignedFile uploadFile(ManifestFile file, String authorizationToken, String partition);

  SignedUrlResult transferFile(URL fileUrl, String authToken, String partition);

  List<Record> failSubmittedFiles(List<IngestedFile> ingestedFiles,
      RequestMeta requestMeta);

  List<Record> failRecords(List<Record> records, RequestMeta requestMeta);
}
