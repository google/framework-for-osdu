package org.opengroup.osdu.core.common.model.legal.validation;

import com.google.common.base.Strings;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NameValidator implements ConstraintValidator<ValidName, String> {

	private static final String NAME_PATTERN = "^[-A-Za-z0-9]{3,100}+$";

	@Override
	public void initialize(ValidName constraintAnnotation) {
		//needed by interface - we don't use
	}

	@Override
	public boolean isValid(String name, ConstraintValidatorContext context) {
		return !Strings.isNullOrEmpty(name) && name.matches(NAME_PATTERN);
	}
}
