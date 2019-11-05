package com.osdu.service.processing;

import com.osdu.model.BaseRecord;

public interface ResultDataProcessor {

  BaseRecord removeRedundantFields(BaseRecord data);
}
