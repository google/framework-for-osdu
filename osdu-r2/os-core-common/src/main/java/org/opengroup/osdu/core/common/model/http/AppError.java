package org.opengroup.osdu.core.common.model.http;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@Builder
public class AppError implements Serializable {
    private static final long serialVersionUID = 2405172041950241677L;
    private int code;
    private String reason;
    private String message;
    @JsonIgnore
    private String[] errors;
    // exclude debuggingInfo & originalException properties in response deserialization as they are not
    // required for swagger endpoint and Portal send weird multipart Content-Type in request
    @JsonIgnore
    private String debuggingInfo;
    @JsonIgnore
    private Exception originalException;

    //AppException creates App Errors with only these 3 attributes
    public AppError(int code, String reason, String message){
        this.code = code;
        this.reason = reason;
        this.message = message;
    }
}

