package com.osdu.service.processing;

import com.osdu.model.BaseRecord;

public interface ResultDataPostProcessor {

  BaseRecord processData(BaseRecord data);
}
