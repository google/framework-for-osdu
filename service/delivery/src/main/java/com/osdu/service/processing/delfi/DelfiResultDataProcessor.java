package com.osdu.service.processing.delfi;

import com.osdu.exception.OsduServerErrorException;
import com.osdu.model.BaseRecord;
import com.osdu.model.FileRecord;
import com.osdu.model.Record;
import com.osdu.model.osdu.delivery.property.OsduDeliveryProperties;
import com.osdu.service.processing.ResultDataProcessor;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DelfiResultDataProcessor implements ResultDataProcessor {

  ProcessFileRecordCommand processFileRecordCommand = new ProcessFileRecordCommand();
  ProcessRecordCommand processRecordCommand = new ProcessRecordCommand();

  final OsduDeliveryProperties properties;

  @Override
  public BaseRecord removeRedundantFields(BaseRecord data) {

    List<Command> commands = Arrays.asList(processFileRecordCommand, processRecordCommand);

    List<Command> commandsToApply = commands.stream()
        .filter(command -> command.isSupported(data)).collect(Collectors.toList());

    if(commandsToApply.size() > 1){
      throw new OsduServerErrorException("There are several commands to apply to that type - " + data);
    }

    return commandsToApply.isEmpty()? data : commandsToApply.get(0).execute(data);
  }

  class ProcessFileRecordCommand implements Command {

    @Override
    public boolean isSupported(BaseRecord data) {
      return data instanceof FileRecord;
    }

    @Override
    public BaseRecord execute(BaseRecord data) {
      ((FileRecord) data).getAdditionalProperties().entrySet()
          .removeIf(entry -> properties.getFieldsToStrip().contains(entry.getKey()));
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
      List<String> fieldsToStrip = properties.getFieldsToStrip();
      record.getData().entrySet().removeIf(entry -> fieldsToStrip.contains(entry.getKey()));
      record.getAdditionalProperties().entrySet()
          .removeIf(entry -> fieldsToStrip.contains(entry.getKey()));
      return data;
    }
  }

  interface Command {

    boolean isSupported(BaseRecord data);

    BaseRecord execute(BaseRecord data);
  }
}

