package com.osdu.service.processing;

import lombok.Data;

@Data
public class ProcessingResult {

    private String srn;
    private boolean isProcessed;
    private String data;

}
