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
