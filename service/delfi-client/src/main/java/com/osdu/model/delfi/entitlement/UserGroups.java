package com.osdu.model.delfi.entitlement;

import java.util.List;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class UserGroups {

  List<Group> groups;
  String memberEmail;
  String desId;

}
