package org.opengroup.osdu.core.common.model.legal;

import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class DataTypeValues {

    public static final String FIRST_PARTY_DATA = "First Party Data";
    public static final String PUBLIC_DOMAIN_DATA = "Public Domain Data";
    public static final String TRANSFERRED_DATA = "Transferred Data";
    public static final String SECOND_PARTY_DATA = "Second Party Data";
    public static final String THIRD_PARTY_DATA = "Third Party Data";

    private static Set<String> sharedDataTypes = Stream.of(FIRST_PARTY_DATA, PUBLIC_DOMAIN_DATA,
            TRANSFERRED_DATA, THIRD_PARTY_DATA, SECOND_PARTY_DATA).collect(Collectors.toSet());

    public Set<String> getDataTypeValues(String complianceRuleSet){
        switch(complianceRuleSet) {
            case TenantInfo.ComplianceRuleSets.SHARED:
                return sharedDataTypes;
            default:
                return Collections.EMPTY_SET;
        }
    }
}
