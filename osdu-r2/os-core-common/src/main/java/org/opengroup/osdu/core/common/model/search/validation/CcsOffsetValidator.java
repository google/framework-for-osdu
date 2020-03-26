// Copyright 2017-2019, Schlumberger
// Copyright 2020 Google LLC
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

import org.opengroup.osdu.core.common.model.search.CcsQueryRequest;
import org.opengroup.osdu.core.common.model.search.QueryRequest;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

// TODO: Remove this temporary implementation when ECE CCS is utilized
public class CcsOffsetValidator implements ConstraintValidator<CcsValidOffset, CcsQueryRequest> {

    @Override
    public void initialize(final CcsValidOffset constraintAnnotation) {
    }

    @Override
    public boolean isValid(CcsQueryRequest ccsQueryRequest, ConstraintValidatorContext context) {
        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setFrom(ccsQueryRequest.getFrom());
        queryRequest.setLimit(ccsQueryRequest.getLimit());
        return new OffsetValidator().isValid(queryRequest, context);
    }
}
