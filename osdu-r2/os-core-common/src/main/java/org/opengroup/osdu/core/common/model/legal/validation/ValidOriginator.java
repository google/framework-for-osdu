package org.opengroup.osdu.core.common.model.legal.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER, ElementType.TYPE, ElementType.TYPE_USE, ElementType.TYPE_PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { OriginatorValidator.class })
@Documented
public @interface ValidOriginator {

	String message() default "Invalid Originator given. Only alphanumeric characters, whitespaces, hyphens and periods are allowed and must be between 3 and 60 characters. Found: ${validatedValue}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
