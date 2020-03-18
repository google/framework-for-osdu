package org.opengroup.osdu.core.common.model.legal.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { SecurityClassificationValidator.class })
@Documented
public @interface ValidSecurityClassification {

    String message() default "Invalid security classification on LegalTag properties. Found: ${validatedValue}.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}