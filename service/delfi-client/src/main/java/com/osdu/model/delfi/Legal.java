package com.osdu.model.delfi;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Legal {

  @JsonProperty("legaltags")
  List<String> legalTags;

  List<String> otherRelevantDataCountries;

  @JsonProperty(access = Access.WRITE_ONLY)
  String status;

}
