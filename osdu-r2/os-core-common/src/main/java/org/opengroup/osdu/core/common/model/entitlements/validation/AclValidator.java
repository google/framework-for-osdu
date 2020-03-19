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

package org.opengroup.osdu.core.common.model.entitlements.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.ArrayUtils;
import org.opengroup.osdu.core.common.model.entitlements.Acl;
import org.opengroup.osdu.core.common.model.storage.validation.ValidationDoc;

public class AclValidator implements ConstraintValidator<ValidAcl, Acl> {

	@Override
	public void initialize(ValidAcl constraintAnnotation) {
		// do nothing
	}

	@Override
	public boolean isValid(Acl acl, ConstraintValidatorContext context) {

		context.disableDefaultConstraintViolation();

		if (ArrayUtils.isEmpty(acl.getViewers())) {
			context.buildConstraintViolationWithTemplate(ValidationDoc.RECORD_ACL_VIEWERS_NOT_EMPTY)
					.addConstraintViolation();
			return false;
		}

		if (ArrayUtils.isEmpty(acl.getOwners())) {
			context.buildConstraintViolationWithTemplate(ValidationDoc.RECORD_ACL_OWNERS_NOT_EMPTY)
					.addConstraintViolation();
			return false;
		}

		for (int i = 0; i < acl.getViewers().length; i++) {
			if (acl.getViewers()[i] == null || !acl.getViewers()[i].matches(ValidationDoc.EMAIL_REGEX)) {
				context.buildConstraintViolationWithTemplate(
						String.format(ValidationDoc.INVALID_GROUP_NAME, acl.getViewers()[i])).addConstraintViolation();
				return false;
			}
		}

		for (int i = 0; i < acl.getOwners().length; i++) {
			if (acl.getOwners()[i] == null || !acl.getOwners()[i].matches(ValidationDoc.EMAIL_REGEX)) {
				context.buildConstraintViolationWithTemplate(
						String.format(ValidationDoc.INVALID_GROUP_NAME, acl.getOwners()[i])).addConstraintViolation();
				return false;
			}
		}

		return true;
	}
}