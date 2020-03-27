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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import com.google.common.base.Strings;
import org.opengroup.osdu.core.common.model.legal.validation.ValidDataType;
import org.opengroup.osdu.core.common.model.legal.validation.ValidExportClassification;
import org.opengroup.osdu.core.common.model.legal.validation.ValidLegalTagProperties;
import org.opengroup.osdu.core.common.model.legal.validation.ValidOriginator;
import org.opengroup.osdu.core.common.model.legal.validation.ValidPersonalData;
import org.opengroup.osdu.core.common.model.legal.validation.ValidSecurityClassification;
import lombok.*;

import  java.sql.Date;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * If any class variable changed here,
 * need to update the corresponding doc model class in SwaggerHelper.java
 */
@Data
@ValidLegalTagProperties
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Properties {
    public static final Date DEFAULT_EXPIRATIONDATE = Date.valueOf("9999-12-31");
    public final static String UNKNOWN_CONTRACT_ID = "Unknown";
    public final static String NO_CONTRACT_ID = "No Contract Related";

    @Setter(AccessLevel.NONE)
    private List<String> countryOfOrigin;

    private String contractId;

    private Date expirationDate;

    @Getter(AccessLevel.NONE)
    @ValidOriginator
    private String originator;

    @ValidDataType
    private String dataType;

    @ValidSecurityClassification
    private String securityClassification;

    @ValidPersonalData
    private String personalData;

    @ValidExportClassification
    private String exportClassification;


    public Properties(){
        countryOfOrigin = new ArrayList<>();
        dataType = "";
        securityClassification = "";
        personalData= "";
        exportClassification = "";
        originator = "";
        contractId = "";
        expirationDate = DEFAULT_EXPIRATIONDATE;
    }

    public void setCountryOfOrigin(List<String> countryOfOrigin) {
        this.countryOfOrigin = countryOfOrigin == null ? null : countryOfOrigin.stream().map(name -> name.toUpperCase()).collect(Collectors.toList()); //expect iso country code so we will enforce uppercase
    }
    public String getOriginator(){
        return originator == null ? "" : originator;
    }

    public void setOriginator(String originator) {
        this.originator = originator.trim();
    }

    @JsonIgnore
    public boolean hasExpired(){
        return expirationDate.before(new Date(System.currentTimeMillis()));
    }
    @JsonIgnore
    public boolean isDefaultExpirationDate(){
        return expirationDate.equals(DEFAULT_EXPIRATIONDATE);
    }
    @JsonIgnore
    public boolean hasThirdPartyDataType(){
        return dataType.equalsIgnoreCase(DataTypeValues.THIRD_PARTY_DATA);
    }
    @JsonIgnore
    public boolean hasSecondPartyDataType(){
        return dataType.equalsIgnoreCase(DataTypeValues.SECOND_PARTY_DATA);
    }
    @JsonIgnore
    public boolean hasContractId(){
        if(Strings.isNullOrEmpty(contractId))
            return false;
        else if(isUnknownOrNonExistantContractId())
            return false;
        else { //validate it has a properties id
            Pattern p = Pattern.compile("^[-.A-Za-z0-9]{3,40}+$");
            return p.matcher(contractId).matches();
        }
    }
    @JsonIgnore
    public boolean isUnknownOrNonExistantContractId() {
        return !Strings.isNullOrEmpty(contractId) && (contractId.equalsIgnoreCase(UNKNOWN_CONTRACT_ID) ||
                contractId.equalsIgnoreCase(NO_CONTRACT_ID));
    }
}
