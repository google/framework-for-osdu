package org.opengroup.osdu.core.common.model.legal;

import java.util.Collection;

public class PersistenceException extends RuntimeException {

    static final long serialVersionUID = -7034897190745766930L;

    private int code;

    private String reason;

    public PersistenceException(int code, String message, String reason) {
        super(message);
        this.code = code;
        this.reason = reason;
    }

    public int getCode(){
        return this.code;
    }

    public String getReason(){
        return this.reason;
    }

}
