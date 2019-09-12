package com.osdu.service.processing;

import com.osdu.model.osdu.delivery.FileRecord;
import com.osdu.model.osdu.delivery.Record;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ResultDataPostProcessor {

    public ResultDataPostProcessor(@Value("${osdu.processing.fields-to-strip}") List<String> fieldsToStrip) {
        this.fieldsToStrip = fieldsToStrip;
        fileRecordPostProcessor = new FileRecordPostProcessor();
        recordPostProcessor = new RecordPostProcessor();
    }

    List<String> fieldsToStrip;
    FileRecordPostProcessor fileRecordPostProcessor;
    RecordPostProcessor recordPostProcessor;

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

