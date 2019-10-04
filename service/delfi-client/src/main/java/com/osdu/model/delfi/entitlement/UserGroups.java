package com.osdu.model.delfi.entitlement;

import java.util.List;
import lombok.Data;

@Data
public class UserGroups {

  List<Group> groups;
  String memberEmail;
  String desId;

}
