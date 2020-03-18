package org.opengroup.osdu.core.common.model.legal;

import lombok.Data;

@Data
public class ListLegalTagArgs {
    private String cursor;
    private int limit;
    private Boolean isValid;
}
