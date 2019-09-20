package com.osdu.model.osdu.delivery.delfi;

import lombok.Data;

@Data
public class ProcessingResult {

    //1 - No Mapping found
    //2 - Reference/Master
    //3 - File url;

    private ProcessingResultStatus processingResultStatus;
    private String fileLocation;
    private Object data;
    private String srn;

}
