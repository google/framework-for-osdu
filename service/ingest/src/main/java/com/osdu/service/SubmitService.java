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

import com.osdu.model.delfi.IngestedFile;
import com.osdu.model.delfi.RequestMeta;
import com.osdu.model.delfi.status.JobsPullingResult;
import com.osdu.model.delfi.submit.SubmitJobResult;
import com.osdu.model.delfi.submit.SubmittedFile;
import java.util.List;
import java.util.Map;

public interface SubmitService {

  JobsPullingResult awaitSubmitJobs(List<String> jobIds, RequestMeta requestMeta);

  SubmitJobResult submitFile(String relativeFilePath, RequestMeta requestMeta);

  List<IngestedFile> getIngestionResult(JobsPullingResult jobsPullingResult,
      Map<String, SubmittedFile> jobIdToFile, RequestMeta requestMeta);

}
