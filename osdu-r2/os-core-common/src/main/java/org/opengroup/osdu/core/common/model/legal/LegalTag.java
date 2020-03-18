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
