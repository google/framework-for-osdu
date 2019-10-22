package com.osdu.service.processing.delfi;

import com.osdu.model.BaseRecord;
import com.osdu.model.FileRecord;
import com.osdu.model.Record;
import com.osdu.model.osdu.delivery.property.OsduDeliveryProperties;
import com.osdu.service.processing.ResultDataService;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DelfiResultDataService implements ResultDataService {

  ProcessFileRecordCommand processFileRecordCommand = new ProcessFileRecordCommand();
  ProcessRecordCommand processRecordCommand = new ProcessRecordCommand();

  final OsduDeliveryProperties properties;

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

