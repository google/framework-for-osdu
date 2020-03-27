
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

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class MultiKindValidator implements ConstraintValidator<ValidMultiKind, String> {

    //tenant1:ihs:well:1.0.0
    private static final String MULTI_KIND_PATTERN = "[\\w-\\.\\*]+:[\\w-\\.\\*]+:[\\w-\\.\\*]+:[(\\d+.)+(\\d+.)+(\\d+)\\*]+$";

    @Override
    public void initialize(ValidMultiKind constraintAnnotation) {
    }

    @Override
    public boolean isValid(String kind, ConstraintValidatorContext context) {

        return kind.matches(MULTI_KIND_PATTERN);
    }
}
