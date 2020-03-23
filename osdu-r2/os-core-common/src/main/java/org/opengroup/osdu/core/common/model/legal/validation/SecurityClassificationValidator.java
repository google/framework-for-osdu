package org.opengroup.osdu.core.common.model.legal.validation;

import org.opengroup.osdu.core.common.model.legal.AllowedLegaltagPropertyValues;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class SecurityClassificationValidator implements ConstraintValidator<ValidSecurityClassification, String> {
    AllowedLegaltagPropertyValues properties = new AllowedLegaltagPropertyValues();

    @Override
    public void initialize(ValidSecurityClassification constraintAnnotation) {
        //needed by interface - we don't use
    }

    @Override
    public boolean isValid(String securityClassification, ConstraintValidatorContext context) {
        if(AllowedLegaltagPropertyValues.SECRET.equalsIgnoreCase(securityClassification)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(AllowedLegaltagPropertyValues.SECRET + " data is currently not allowed. Please do not upload this.")
                    .addConstraintViolation();
            return false;
        }

        return securityClassification == null ? false : properties.getSecurityClassifications().stream().anyMatch(securityClassification::equalsIgnoreCase);
    }
}
