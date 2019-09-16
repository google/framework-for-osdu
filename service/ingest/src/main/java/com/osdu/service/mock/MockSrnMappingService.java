package com.osdu.service.mock;

import com.osdu.model.SchemaData;
import com.osdu.service.SrnMappingService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockSrnMappingService implements SrnMappingService {

  private Map<String, String> srnToKindMap;
  private Map<String, List<SchemaData>> srnToSchemaDataMap;

  public MockSrnMappingService() {
    //TODO: Fill in mock data here
    srnToKindMap = new HashMap<>();
    srnToSchemaDataMap = new HashMap<>();
  }

  @Override
  public String mapSRNToKind(String srn) {
    return null;
  }

  @Override
  public List<SchemaData> getSchemaDataForSrn(String srn) {
    return null;
  }
}
