package org.opengroup.osdu.core.common.model.legal.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER, ElementType.TYPE, ElementType.TYPE_USE, ElementType.TYPE_PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { DescriptionValidator.class })
@Documented
public @interface ValidDescription {

	String message() default "Invalid description given. It needs to be less than or equal to 380 characters.";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
