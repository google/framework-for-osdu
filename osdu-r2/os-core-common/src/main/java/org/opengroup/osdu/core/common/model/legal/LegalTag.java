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

package org.opengroup.osdu.core.common.model.legal;

import org.opengroup.osdu.core.common.model.legal.validation.ValidDescription;
import org.opengroup.osdu.core.common.model.legal.validation.ValidName;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import javax.validation.Valid;

@Data
public class LegalTag {
    private Long id;

    @ValidName
    private String name;

    @Setter(AccessLevel.NONE)
    @ValidDescription
    private String description;

    @Valid
    private Properties properties;

    private Boolean isValid;

    public LegalTag(){
        description = "";
        name = "";
        id = -1L;
        isValid = false;
        properties = new Properties();
    }

    public void setDescription(String value){
        description = value == null ? "" : value;
    }
    public void setDefaultId(){
        id = (long) name.hashCode();
    }
    public static long getDefaultId(String name){
        return name.hashCode();
    }
}
