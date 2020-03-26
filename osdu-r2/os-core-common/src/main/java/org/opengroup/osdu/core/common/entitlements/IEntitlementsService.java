// Copyright 2017-2019, Schlumberger
// Copyright 2020 Google LLC
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

package org.opengroup.osdu.core.common.entitlements;

import org.opengroup.osdu.core.common.model.entitlements.EntitlementsException;
import org.opengroup.osdu.core.common.model.entitlements.CreateGroup;
import org.opengroup.osdu.core.common.model.entitlements.GetMembers;
import org.opengroup.osdu.core.common.model.entitlements.GroupEmail;
import org.opengroup.osdu.core.common.model.entitlements.GroupInfo;
import org.opengroup.osdu.core.common.model.entitlements.Groups;
import org.opengroup.osdu.core.common.model.entitlements.MemberInfo;
import org.opengroup.osdu.core.common.model.entitlements.Members;

public interface IEntitlementsService {

    MemberInfo addMember(GroupEmail groupEmail, MemberInfo memberInfo) throws EntitlementsException;

    Members getMembers(GroupEmail groupEmail, GetMembers getMembers) throws EntitlementsException;

    Groups getGroups() throws EntitlementsException;

    GroupInfo createGroup(CreateGroup group) throws EntitlementsException;

    void deleteMember(String groupEmail, String memberEmail) throws EntitlementsException;

    Groups authorizeAny(String... groupNames) throws EntitlementsException;

    void authenticate() throws EntitlementsException;
}
