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

package org.opengroup.osdu.core.common.model.storage.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class KindValidator implements ConstraintValidator<ValidKind, String> {


	@Override
	public void initialize(ValidKind constraintAnnotation) {
		// do nothing
	}

	@Override
	public boolean isValid(String kind, ConstraintValidatorContext context) {

		return kind.matches(ValidationDoc.KIND_REGEX);
	}

	public static boolean isKindFromTenantValid(String kind, String tenant) {

		String kindAccount = kind.split(":")[0];

		return kind.matches(ValidationDoc.KIND_REGEX) && kindAccount.equalsIgnoreCase(tenant);
	}
}