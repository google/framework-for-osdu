package com.osdu.model.delfi.submit;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

@Data
@Builder
public class Acl {

  @Singular
  List<String> owners;

  @Singular
  List<String> viewers;

}
