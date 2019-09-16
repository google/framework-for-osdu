package com.osdu.service;

import com.osdu.model.SchemaData;
import java.util.List;

public interface SrnMappingService {
  String mapSRNToKind(String srn);
  List<SchemaData> getSchemaDataForSrn(String srn);
}
