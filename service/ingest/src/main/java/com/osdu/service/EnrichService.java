package com.osdu.service;

import com.osdu.model.Record;
import com.osdu.model.delfi.RequestMeta;
import com.osdu.model.delfi.submit.SubmittedFile;
import org.springframework.messaging.MessageHeaders;

public interface EnrichService {

  Record enrichRecord(SubmittedFile submittedFile, RequestMeta requestMeta, MessageHeaders headers);

}
