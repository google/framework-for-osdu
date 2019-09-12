package com.osdu.service.processing.delfi;

import com.osdu.model.osdu.delivery.FileRecord;
import com.osdu.model.osdu.delivery.Record;
import com.osdu.service.processing.ResultDataPostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DelfiResultDataPostProcessor implements ResultDataPostProcessor {

    public DelfiResultDataPostProcessor(@Value("${osdu.processing.fields-to-strip}") List<String> fieldsToStrip) {
        this.fieldsToStrip = fieldsToStrip;
        fileRecordPostProcessor = new FileRecordPostProcessor();
        recordPostProcessor = new RecordPostProcessor();
    }

    private List<String> fieldsToStrip;
    private FileRecordPostProcessor fileRecordPostProcessor;
    private RecordPostProcessor recordPostProcessor;

    private final static Logger log = LoggerFactory.getLogger(DelfiResultDataPostProcessor.class);

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

