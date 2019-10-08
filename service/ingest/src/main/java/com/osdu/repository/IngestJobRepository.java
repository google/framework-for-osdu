package com.osdu.repository;

import com.osdu.model.job.IngestJob;
import java.util.Map;

public interface IngestJobRepository {

  IngestJob findById(String id);

  void save(IngestJob ingestJob);

  void updateFields(String id, Map<String, Object> fields);
}
