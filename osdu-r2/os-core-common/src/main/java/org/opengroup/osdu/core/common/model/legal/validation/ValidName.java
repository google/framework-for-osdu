package org.opengroup.osdu.core.common.model.legal.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;


@Target({ ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER, ElementType.TYPE, ElementType.TYPE_USE, ElementType.TYPE_PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { NameValidator.class })
@Documented
public @interface ValidName {

	String message() default "Invalid name given. It needs to be between 3 and 100 characters and only alphanumeric characters and hyphens allowed e.g. 'usa-public'. Found: ${validatedValue}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
