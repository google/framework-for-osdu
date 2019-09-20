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
        srnToKindMap.put("no-location-example", "systemdefault2pr6ah6z50:doc:7cea9dc855e84778a881f8c13d4625db");
        srnToKindMap.put("location-example", "systemdefault2pr6ah6z50:doc:ae7a2aad9aa64672b79ebd4365bc7772");
    }

    @Override
    public String mapSRNToKind(String srn) {
        return srnToKindMap.get(srn);
    }
}
