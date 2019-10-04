package com.osdu.service;

import com.osdu.model.delfi.RequestMeta;
import com.osdu.model.delfi.status.JobsPullingResult;
import com.osdu.model.delfi.submit.SubmitJobResult;
import java.util.List;

public interface SubmitService {

  JobsPullingResult awaitSubmitJobs(List<String> jobIds, RequestMeta requestMeta);

  SubmitJobResult submitFile(String relativeFilePath, RequestMeta requestMeta);

}
