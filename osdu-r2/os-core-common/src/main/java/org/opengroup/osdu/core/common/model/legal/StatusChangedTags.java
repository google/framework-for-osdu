package org.opengroup.osdu.core.common.model.legal;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class StatusChangedTags {
    private List<StatusChangedTag> statusChangedTags = new ArrayList<>();
}
