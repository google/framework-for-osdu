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

package org.opengroup.osdu.core.common.model.legal.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.math.NumberUtils;

import org.opengroup.osdu.core.common.model.storage.Record;

import io.jsonwebtoken.lang.Collections;
import org.opengroup.osdu.core.common.model.storage.validation.ValidationDoc;

public class LegalValidator implements ConstraintValidator<ValidLegal, Record> {

	@Override
	public void initialize(ValidLegal constraintAnnotation) {
		// do nothing
	}

	@Override
	public boolean isValid(Record record, ConstraintValidatorContext context) {

		context.disableDefaultConstraintViolation();

		if (record.getAncestry() != null && !Collections.isEmpty(record.getAncestry().getParents())) {
			for (String parent : record.getAncestry().getParents()) {
				String[] tokens = parent.split(":");

				if (tokens.length != 4) {
					String msg = String.format(ValidationDoc.INVALID_PARENT_RECORD_ID_FORMAT, parent);

					context.buildConstraintViolationWithTemplate(msg).addConstraintViolation();
					return false;
				}

				if (!NumberUtils.isCreatable(tokens[tokens.length - 1])) {
					String msg = String.format(ValidationDoc.INVALID_PARENT_RECORD_VERSION_FORMAT, parent);

					context.buildConstraintViolationWithTemplate(msg).addConstraintViolation();
					return false;
				}
			}
			return true;
		}

		if (Collections.isEmpty(record.getLegal().getLegaltags())) {
			context.buildConstraintViolationWithTemplate(ValidationDoc.RECORD_LEGAL_TAGS_NOT_EMPTY)
					.addConstraintViolation();
			return false;
		}

		return true;
	}
}