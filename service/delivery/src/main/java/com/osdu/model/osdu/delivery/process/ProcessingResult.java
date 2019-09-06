package com.osdu.model.osdu.delivery.process;

import lombok.Data;

@Data
public class ProcessingResult {

    private String fileLocation;
    private Object data;
    private String srn;

    private boolean isProcessed;

}
