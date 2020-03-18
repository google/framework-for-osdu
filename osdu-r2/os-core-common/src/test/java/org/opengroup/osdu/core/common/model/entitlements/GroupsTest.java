// Copyright 2017-2019, Schlumberger
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.core.common.model.entitlements;

import org.junit.Test;
import org.opengroup.osdu.core.common.model.entitlements.GroupInfo;
import org.opengroup.osdu.core.common.model.entitlements.Groups;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class GroupsTest {
    @Test
    public void should_returnTrue_when_groupsContainsPermission() {
        Groups sut = this.generateSut();

        assertTrue(sut.any("group.2"));
    }

    @Test
    public void should_returnFalse_when_groupsDoesNotContainsPermission() {
        Groups sut = this.generateSut();

        assertFalse(sut.any("group.4"));
    }

    @Test
    public void should_returnFalse_when_groupsContainsPermissionInDifferentCase() {
        Groups sut = this.generateSut();

        assertFalse(sut.any("GrouP.2"));
    }

    @Test
    public void should_returnTrue_when_groupsContainsOnePermission() {
        Groups sut = this.generateSut();

        assertTrue(sut.any("GrouP.4", "group.1"));
    }

    @Test
    public void should_returnFalse_when_groupsContainsNull() {
        Groups sut = this.generateSut();
        sut.setGroups(null);

        assertFalse(sut.any("group.1"));
    }

    @Test
    public void should_getGroupNames_when_groupsExist() {
        Groups sut = this.generateSut();
        List<String> result = sut.getGroupNames();
        assertEquals(3, result.size());
        assertEquals("group.1", result.get(0));
        assertEquals("group.2", result.get(1));
        assertEquals("group.3", result.get(2));
    }

    @Test
    public void should_returnGroupInfo_when_groupsContainsGroupName() {
        Groups sut = this.generateSut();

        assertEquals("group.1", sut.getGroup("group.1").getName());
    }

    @Test
    public void should_returnNull_when_groupsDoesNotContainGroupName() {
        Groups sut = this.generateSut();

        assertNull(sut.getGroup("group.43"));
    }

    Groups generateSut() {
        Groups output = new Groups();
        GroupInfo group1 = new GroupInfo();
        group1.setName("group.1");
        output.getGroups().add(group1);

        GroupInfo group2 = new GroupInfo();
        group2.setName("group.2");
        output.getGroups().add(group2);

        GroupInfo group3 = new GroupInfo();
        group3.setName("group.3");
        output.getGroups().add(group3);

        return output;
    }
}