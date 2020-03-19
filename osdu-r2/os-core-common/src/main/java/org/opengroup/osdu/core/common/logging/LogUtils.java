package org.opengroup.osdu.core.common.logging;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class LogUtils {

    public static final String TRACE_CONTEXT_KEY = "x-cloud-trace-context";
    public static final String COR_ID = "correlation-id";
    public static final String ACCOUNT_ID = "account-id";
    public static final String DATA_PARTITION_ID = "data-partition-id";
    private static final HashSet<String> headerKeys = new HashSet<>();

    static {
        headerKeys.add(ACCOUNT_ID);
        headerKeys.add(DATA_PARTITION_ID);
        headerKeys.add(COR_ID);
        headerKeys.add(TRACE_CONTEXT_KEY);
    }

    public static Map<String, String> createStandardLabelsFromEntrySet(Set<Map.Entry<String, List<String>>> input){
        Map<String, String> output = new HashMap<>();
        if(input != null) {
            input.forEach((k) -> {
                String key = k.getKey().toLowerCase();
                if (headerKeys.contains(key))
                    output.put(key, StringUtils.join(k.getValue(), ','));
            });
        }
        return output;
    }

    public static Map<String, String> createStandardLabelsFromMap(Map<String, String> input){
        Map<String, String> output = new HashMap<>();
        if(input != null) {
            input.forEach((k, v) -> {
                String key = k.toLowerCase();
                if (headerKeys.contains(key))
                    output.put(key, v);
            });
        }
        return output;
    }
}
