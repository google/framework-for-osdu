package com.osdu.service.mock;

import com.osdu.service.SrnMappingService;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

/**
 * Temporary service to return Kinds while mapping is not yet implemented.
 */
@Service
@Slf4j
public class MockSrnMappingService implements SrnMappingService {

  Map<String, String> srnToKindMap;

  /**
   * Constructor for MockSrnMappingService.
   */
  public MockSrnMappingService() {
    srnToKindMap = new HashMap<>();
    srnToKindMap.put("sampleSrn", "sampleKind");
    srnToKindMap.put("yetAnotherSrn", "common:welldb:wellbore-dataset_descriptor-1.5");
    srnToKindMap
        .put("no-location-example", "systemdefault2pr6ah6z50:doc:7cea9dc855e84778a881f8c13d4625db");
    srnToKindMap
        .put("location-example", "systemdefault2pr6ah6z50:doc:ae7a2aad9aa64672b79ebd4365bc7772");
  }

  @Override
  public String mapSrnToKind(String srn) {
    log.debug("MOCK SERVICE : Mapping srn to kind : {}, result: {}", srn, srnToKindMap.get("srn"));
    return srnToKindMap.get(srn);
  }
}
