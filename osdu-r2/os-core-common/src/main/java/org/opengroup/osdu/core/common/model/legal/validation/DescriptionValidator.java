package org.opengroup.osdu.core.common.model.legal.validation;

import com.google.common.base.Strings;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class DescriptionValidator implements ConstraintValidator<ValidDescription, String> {

	@Override
	public void initialize(ValidDescription constraintAnnotation) {
		//needed by interface - we don't use
	}

	@Override
	public boolean isValid(String description, ConstraintValidatorContext context) {
		return Strings.isNullOrEmpty(description) || description.length() <= 380;
	}
}
