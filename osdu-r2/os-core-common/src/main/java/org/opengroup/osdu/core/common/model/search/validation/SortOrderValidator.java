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

package org.opengroup.osdu.core.common.model.search.validation;

import org.opengroup.osdu.core.common.SwaggerDoc;
import org.opengroup.osdu.core.common.model.search.SortOrder;
import org.opengroup.osdu.core.common.model.search.SortQuery;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

public class SortOrderValidator implements ConstraintValidator<ValidSortOrder, SortQuery> {

    @Override
    public void initialize(final ValidSortOrder constraintAnnotation) {
    }

    @Override
    public boolean isValid(SortQuery sort, ConstraintValidatorContext context) {
        if (sort == null) {
            return true;
        }
        List<String> field = sort.getField();
        List<SortOrder> order = sort.getOrder();
        if (isBlank(field)) {
            return getViolation(context, SwaggerDoc.SORT_FIELD_VALIDATION_NOT_EMPTY_MSG);
        }
        if (isBlank(order)) {
            return getViolation(context, SwaggerDoc.SORT_ORDER_VALIDATION_NOT_EMPTY_MSG);
        }
        if (field.size() != order.size()) {
            return getViolation(context, SwaggerDoc.SORT_FIELD_ORDER_SIZE_NOT_MATCH);
        }
        if (field.stream().filter(val -> (val == null || val.trim().isEmpty())).count() > 0) {
            return getViolation(context, SwaggerDoc.SORT_FIELD_LIST_VALIDATION_NOT_EMPTY_MSG);
        }
        if (order.stream().filter(val -> (val == null)).count() > 0) {
            return getViolation(context, SwaggerDoc.SORT_NOT_VALID_ORDER_OPTION);
        }
        return true;
    }

    private boolean isBlank(List<?> list) {
        return list == null || list.isEmpty();
    }

    private boolean getViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
        return false;
    }
}
