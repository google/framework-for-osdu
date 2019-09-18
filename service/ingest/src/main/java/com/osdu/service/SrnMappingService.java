package com.osdu.service;

import com.osdu.model.SchemaData;
import java.util.List;

public interface SrnMappingService {
  String mapSRNToKind(String srn);
  SchemaData getSchemaDataForSrn(String srn);
}
