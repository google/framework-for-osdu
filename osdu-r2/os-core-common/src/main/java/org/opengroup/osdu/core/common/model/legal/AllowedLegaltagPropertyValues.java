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

package org.opengroup.osdu.core.common.model.legal;

import org.opengroup.osdu.core.common.model.http.RequestInfo;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class AllowedLegaltagPropertyValues {
    @Inject
    DataTypeValues dataTypeValues;
    @Inject
    RequestInfo requestInfo;

    public static final String PUBLIC = "Public";
    public static final String PRIVATE = "Private";
    public static final String CONFIDENTIAL = "Confidential";
    public static final String SECRET = "Secret";
    private static Set<String> securityClassifications = Stream.of(PUBLIC,PRIVATE,CONFIDENTIAL)
            .collect(Collectors.toSet());

    public static final String EAR99 = "EAR99";
    public static final String NOT_TECHNICAL_DATA = "Not - Technical Data";
    public static final String NO_LICENSE_REQUIRED = "No License Required";
    private static Set<String> eccns = Stream.of(EAR99, NOT_TECHNICAL_DATA, NO_LICENSE_REQUIRED)
            .collect(Collectors.toSet());

    public static final String PERSONALLY_IDENTIFIABLE = "Personally Identifiable";
    public static final String NO_PERSONAL_DATA = "No Personal Data";
    private static Set<String> personalDataType = Stream.of(PERSONALLY_IDENTIFIABLE, NO_PERSONAL_DATA)
            .collect(Collectors.toSet());

    public Set<String> getSecurityClassifications(){
        return securityClassifications;
    }
    public Set<String> getEccns(){
        return eccns;
    }
    public Set<String> getPersonalDataType(){
        return personalDataType;
    }
    public Set<String> getDataTypes(){
        String ruleSet = requestInfo.getComplianceRuleSet();
        return dataTypeValues.getDataTypeValues(ruleSet);
    }
}
