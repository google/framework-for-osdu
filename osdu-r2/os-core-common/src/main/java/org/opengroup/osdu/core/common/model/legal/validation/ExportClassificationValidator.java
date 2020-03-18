package org.opengroup.osdu.core.common.model.legal.validation;

import com.google.common.base.Strings;
import org.opengroup.osdu.core.common.model.legal.AllowedLegaltagPropertyValues;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ExportClassificationValidator implements ConstraintValidator<ValidExportClassification, String> {

    AllowedLegaltagPropertyValues properties = new AllowedLegaltagPropertyValues();

    @Override
    public void initialize(ValidExportClassification constraintAnnotation) {
        //needed by interface - we don't use
    }

    @Override
    public boolean isValid(String exportClassification, ConstraintValidatorContext context) {
        if(Strings.isNullOrEmpty(exportClassification))
            return false;
        else
            return properties.getEccns().stream().anyMatch(exportClassification::equalsIgnoreCase);


    }
}
