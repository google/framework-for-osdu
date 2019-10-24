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
