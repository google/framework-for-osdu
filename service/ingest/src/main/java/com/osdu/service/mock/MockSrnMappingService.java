package com.osdu.service.mock;

import com.osdu.model.SchemaData;
import com.osdu.service.SrnMappingService;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class MockSrnMappingService implements SrnMappingService {

  Map<String, String> srnToKindMap;
  Map<String, SchemaData> srnToSchemaDataMap;

  public MockSrnMappingService() {
    srnToKindMap = new HashMap<>();
    srnToKindMap.put("valid-srn-example", "valid-kind-result");
    srnToKindMap.put("srn-with-no-schema", "kind-for-srn-with-no-schema");
    srnToSchemaDataMap = new HashMap<>();
    srnToSchemaDataMap.put("valid-srn-example", new SchemaData());
  }

  @Override
  public String mapSRNToKind(String srn) {
    return srnToKindMap.get(srn);
  }

  @Override
  public SchemaData getSchemaDataForSrn(String srn) {
    return srnToSchemaDataMap.get(srn);
  }
}
