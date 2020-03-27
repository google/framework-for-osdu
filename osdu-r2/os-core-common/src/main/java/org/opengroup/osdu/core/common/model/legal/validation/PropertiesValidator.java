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

package org.opengroup.osdu.core.common.model.legal.validation;

import org.opengroup.osdu.core.common.model.legal.Properties;
import org.opengroup.osdu.core.common.model.legal.validation.rules.Rule;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

//This will hold validation for the Properties model for the properties that rely on other property values for their validation rules
//any properties in this model that do not rely on other properties will have their own validator
public class PropertiesValidator implements ConstraintValidator<ValidLegalTagProperties, Properties> {

    private final List<Rule> ruleSet;

    public PropertiesValidator(List<Rule> ruleSet){
        this.ruleSet = ruleSet;
    }

    @Override
    public void initialize(ValidLegalTagProperties constraintAnnotation) {
        //needed by interface - we don't use
    }

    @Override
    public boolean isValid(Properties legalTagProperties, ConstraintValidatorContext context) {
        for (Rule rule : ruleSet) {
            if(rule.shouldCheck(legalTagProperties)) {
                if (!rule.isValid(legalTagProperties, context)) {
                    return false;
                }
            }
        }
        return true;
    }
}
