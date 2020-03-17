/*
 * Copyright 2020 Google LLC
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

package org.opengroup.osdu.core.common.model.legal.validation.rules;

import com.google.common.base.Strings;
import org.opengroup.osdu.core.common.model.legal.Properties;

import javax.validation.ConstraintValidatorContext;

public abstract class Rule {
    public abstract boolean shouldCheck(Properties properties);
    public boolean isValid(Properties properties, ConstraintValidatorContext context){
        String errors = hasError(properties);
        if(Strings.isNullOrEmpty(errors))
            return true;
        else
            return createError(context, errors);
    }

    protected abstract String hasError(Properties properties);
    private boolean createError(ConstraintValidatorContext context, String msg) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(msg)
                .addConstraintViolation();
        return false;
    }

}
