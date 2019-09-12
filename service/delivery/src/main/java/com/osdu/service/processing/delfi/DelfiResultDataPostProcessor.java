package com.osdu.service.processing.delfi;

import com.osdu.model.osdu.delivery.FileRecord;
import com.osdu.model.osdu.delivery.Record;
import com.osdu.service.processing.ResultDataPostProcessor;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DelfiResultDataPostProcessor implements ResultDataPostProcessor {

  /**
   * Constructor for DelfiResultDataPostProcessor.
   * @param fieldsToStrip fields to remove from input
   */
  public DelfiResultDataPostProcessor(
      @Value("${osdu.processing.fields-to-strip}") List<String> fieldsToStrip) {
    this.fieldsToStrip = fieldsToStrip;
    fileRecordPostProcessor = new FileRecordPostProcessor();
    recordPostProcessor = new RecordPostProcessor();
  }

  List<String> fieldsToStrip;
  FileRecordPostProcessor fileRecordPostProcessor;
  RecordPostProcessor recordPostProcessor;

  @Override
  public Object processData(Object data) {

    if (data instanceof FileRecord) {
      fileRecordPostProcessor.processData((FileRecord) data);
    } else if (data instanceof Record) {
      recordPostProcessor.processData((Record) data);
    } else {
      log.info("No data post processor defined for result data type");
    }
    return data;
  }

  class FileRecordPostProcessor {

    FileRecord processData(FileRecord data) {
      data.getDetails().entrySet().removeIf(entry -> fieldsToStrip.contains(entry.getKey()));
      return data;
    }
  }

  class RecordPostProcessor {

    Record processData(Record data) {
      if (data.getData() != null) {
        data.getData().entrySet().removeIf(entry -> fieldsToStrip.contains(entry.getKey()));
      }
      data.getDetails().entrySet().removeIf(entry -> fieldsToStrip.contains(entry.getKey()));
      return data;
    }
  }
}

