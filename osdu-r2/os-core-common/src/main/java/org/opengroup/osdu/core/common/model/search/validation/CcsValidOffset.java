/*
 * Copyright 2020 Google LLC
 * Copyright 2017-2019, Schlumberger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.core.common.model.search.validation;



import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

// TODO: Remove this temporary implementation when ECE CCS is utilized
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {CcsOffsetValidator.class})
@Documented
public @interface CcsValidOffset {

    String message() default "Not a valid sum of offset + limit value";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
