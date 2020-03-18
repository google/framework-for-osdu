package org.opengroup.osdu.core.common.model.legal.validation;

import com.google.common.base.Strings;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class OriginatorValidator implements ConstraintValidator<ValidOriginator, String> {

	private static final String PATTERN = "^[-. A-Za-z0-9]{3,60}+$";

	@Override
	public void initialize(ValidOriginator constraintAnnotation) {
		//needed by interface - we don't use
	}

	@Override
	public boolean isValid(String originator, ConstraintValidatorContext context) {
		return !isNullOrWhitespace(originator) && originator.matches(PATTERN);
	}

	private boolean isNullOrWhitespace(String string){
		return Strings.isNullOrEmpty(string) || string.trim().length() == 0;
	}
}
