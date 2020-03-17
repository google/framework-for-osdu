/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.core.common.model.entitlements;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class Groups {
    private List<GroupInfo> groups = new ArrayList<>();
    private String memberEmail;
    private String desId;

    public List<String> getGroupNames() {
        if (this.groups == null) {
            return new ArrayList<>();
        }

        return this.groups.stream().map(GroupInfo::getName).collect(Collectors.toList());
    }

    public Boolean any(String... groupNames) {
        Boolean output = false;
        if (this.groups != null) {
            for (String groupName : groupNames) {
                if (this.groups.stream()
                        .map(GroupInfo::getName)
                        .anyMatch(p -> p.equals(groupName))) {
                    output = true;
                    break;
                }
            }
        }

        return output;
    }

    public GroupInfo getGroup(String groupName) {
        for (GroupInfo group : this.groups) {
            if (group.getName().equals(groupName)) {
                return group;
            }
        }
        return null;
    }
}
