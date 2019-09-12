package com.osdu.service.processing.delfi;

import com.osdu.model.osdu.delivery.FileRecord;
import com.osdu.model.osdu.delivery.Record;
import com.osdu.service.processing.ResultDataPostProcessor;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DelfiResultDataPostProcessor implements ResultDataPostProcessor {

  /**
   * Constructor for DelfiResultDataPostProcessor.
   *
   * @param fieldsToStrip fields to remove from input
   */
  public DelfiResultDataPostProcessor(
      @Value("${osdu.processing.fields-to-strip}") List<String> fieldsToStrip) {
    this.fieldsToStrip = fieldsToStrip;
    processFileRecordCommand = new ProcessFileRecordCommand();
    processRecordCommand = new ProcessRecordCommand();
  }

  List<String> fieldsToStrip;
  ProcessFileRecordCommand processFileRecordCommand;
  ProcessRecordCommand processRecordCommand;

  @Override
  public Object processData(Object data) {

    if (data instanceof FileRecord) {
      return processFileRecordCommand.execute(data);
    }
    if (data instanceof Record) {
      return processRecordCommand.execute(data);
    }
    log.info("No data post processor defined for result data type");

    return data;
  }

  class ProcessFileRecordCommand implements Command {

    @Override
    public Object execute(Object data) {
      ((FileRecord) data).getDetails().entrySet()
          .removeIf(entry -> fieldsToStrip.contains(entry.getKey()));
      return data;
    }
  }

  class ProcessRecordCommand implements Command {

    @Override
    public Object execute(Object data) {
      Record record = (Record) data;
      if (record.getData() != null) {
        record.getData().entrySet().removeIf(entry -> fieldsToStrip.contains(entry.getKey()));
      }
      record.getDetails().entrySet().removeIf(entry -> fieldsToStrip.contains(entry.getKey()));
      return data;
    }
  }

  interface Command {

    Object execute(Object data);
  }
}

