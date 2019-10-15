package com.osdu.service.processing.delfi;

import com.osdu.model.BaseRecord;
import com.osdu.model.FileRecord;
import com.osdu.model.Record;
import com.osdu.service.processing.ResultDataPostProcessor;
import java.util.Arrays;
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
  public BaseRecord processData(BaseRecord data) {

    List<Command> commands = Arrays.asList(processFileRecordCommand, processRecordCommand);

    return commands.stream()
        .filter(command -> command.isSupported(data))
        .findFirst()
        .map(command -> command.execute(data))
        .orElse(data);
  }

  class ProcessFileRecordCommand implements Command {

    @Override
    public boolean isSupported(BaseRecord data) {
      return data instanceof FileRecord;
    }

    @Override
    public BaseRecord execute(BaseRecord data) {
      ((FileRecord) data).getDetails().entrySet()
          .removeIf(entry -> fieldsToStrip.contains(entry.getKey()));
      return data;
    }
  }

  class ProcessRecordCommand implements Command {

    @Override
    public boolean isSupported(BaseRecord data) {
      return data instanceof Record;
    }

    @Override
    public BaseRecord execute(BaseRecord data) {
      Record record = (Record) data;
      if (record.getData() != null) {
        record.getData().entrySet().removeIf(entry -> fieldsToStrip.contains(entry.getKey()));
      }
      record.getDetails().entrySet().removeIf(entry -> fieldsToStrip.contains(entry.getKey()));
      return data;
    }
  }

  interface Command {

    boolean isSupported(BaseRecord data);

    BaseRecord execute(BaseRecord data);
  }
}

