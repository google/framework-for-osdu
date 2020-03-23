package org.opengroup.osdu.core.common.model.http;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;

@Data
@Builder
public class Request {
    @Builder.Default
    private String requestMethod="";
    @Builder.Default
    private Duration latency = Duration.ofSeconds(0);
    @Builder.Default
    private String requestUrl = "";
    private int Status;
    @Builder.Default
    private String ip = "";
}
