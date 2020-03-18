package org.opengroup.osdu.core.common.model.legal;

import lombok.Data;

@Data
public class StatusChangedTag {
    private String changedTagName;
    private Enum changedTagStatus;

    StatusChangedTag(){
    }
    public StatusChangedTag(String changedTagName, Enum changedTagStatus) {
        this.changedTagName = changedTagName;
        this.changedTagStatus = changedTagStatus;
    }
}
