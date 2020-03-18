package org.opengroup.osdu.core.common.model.legal.validation;

import org.opengroup.osdu.core.common.model.legal.AllowedLegaltagPropertyValues;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PersonalDataValidator implements ConstraintValidator<ValidPersonalData, String> {
    AllowedLegaltagPropertyValues properties = new AllowedLegaltagPropertyValues();

    @Override
    public void initialize(ValidPersonalData constraintAnnotation) {
        //needed by interface - we don't use
    }

    @Override
    public boolean isValid(String personalData, ConstraintValidatorContext context) {
        return personalData == null ? false : properties.getPersonalDataType().stream().anyMatch(personalData::equalsIgnoreCase);
    }
}
