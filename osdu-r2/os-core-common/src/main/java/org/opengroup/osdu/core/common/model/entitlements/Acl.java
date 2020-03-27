/*
 * Copyright 2020 Google LLC
 * Copyright 2017-2019, Schlumberger
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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Acl {

    public String[] viewers;

    public String[] owners;

    public static String[] flattenAcl(Acl acl) {
        Set<String> xAcl = new HashSet<>();
        if (acl.getOwners() != null && acl.getOwners().length > 0) xAcl.addAll(Arrays.asList(acl.getOwners()));
        if (acl.getViewers() != null && acl.getViewers().length > 0) xAcl.addAll(Arrays.asList(acl.getViewers()));
        return xAcl.toArray(new String[xAcl.size()]);
    }
}
