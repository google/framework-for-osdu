package org.opengroup.osdu.core.common.provider.interfaces;


import org.opengroup.osdu.core.common.model.http.DpsHeaders;

import java.util.Map;

public interface IRequestInfo {
    public DpsHeaders getHeaders();

    public String getPartitionId();

    public Map<String, String> getHeadersMap();

    public Map<String, String> getHeadersMapWithDwdAuthZ();

    public DpsHeaders getHeadersWithDwdAuthZ();


    public boolean isCronRequest();

    public boolean isTaskQueueRequest();
}
