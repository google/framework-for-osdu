package com.osdu.model.delfi;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Acl {

  @Singular
  List<String> owners;

  @Singular
  List<String> viewers;

}
