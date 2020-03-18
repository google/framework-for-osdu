// Copyright 2017-2019, Schlumberger
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.core.common.model.search.validation;

import org.opengroup.osdu.core.common.model.search.QueryRequest;
import org.opengroup.osdu.core.common.model.search.QueryUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class OffsetValidator implements ConstraintValidator<ValidOffset, QueryRequest> {

    /**
     * This is for the default elastic settings to have a maximum limit of 10000 for offset and limit combination.
     * If the settings change, this validator needs to be changed
     */
    private static final int offsetLimit = 10000;

    @Override
    public void initialize(final ValidOffset constraintAnnotation) {
    }

    @Override
    public boolean isValid(QueryRequest queryRequest, ConstraintValidatorContext context) {
        int offset = queryRequest.getFrom();
        int limit = QueryUtils.getResultSizeForQuery(queryRequest.getLimit());

        if (offset + limit <= offsetLimit) {
            return true;
        } else {
            String message = String.format("Invalid combination of limit and offset values, offset + limit cannot be greater than %s", offsetLimit);
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
        }
        return false;
    }
}
