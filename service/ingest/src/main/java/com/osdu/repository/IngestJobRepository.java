package com.osdu.repository;

import com.osdu.model.job.IngestJob;

public interface IngestJobRepository {

  IngestJob findById(String id);

  void save(IngestJob ingestJob);
}
