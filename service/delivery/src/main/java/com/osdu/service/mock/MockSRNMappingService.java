package com.osdu.service.mock;

import com.osdu.service.SRNMappingService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Temporary service to return Kinds while mapping is not yet implemented
 */
@Service
public class MockSRNMappingService implements SRNMappingService {

    private Map<String, String> srnToKindMap;

    public MockSRNMappingService() {
        srnToKindMap = new HashMap<>();
        srnToKindMap.put("sampleSrn", "sampleKind");
        srnToKindMap.put("yetAnotherSrn", "common:welldb:wellbore-dataset_descriptor-1.5");
    }

    @Override
    public String mapSRNToKind(String srn) {
        return srnToKindMap.get(srn);
    }
}
