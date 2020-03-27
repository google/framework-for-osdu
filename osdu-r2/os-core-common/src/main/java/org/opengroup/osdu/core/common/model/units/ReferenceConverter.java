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

package org.opengroup.osdu.core.common.model.units;

import org.opengroup.osdu.core.common.model.units.impl.PersistableReference;

import static org.opengroup.osdu.core.common.model.units.ItemFactory.createModel;

/**
 * Factory to create {@link IUnit} instances given a persistable reference {@link String}.
 */
public final class ReferenceConverter {
    private static final String V2_START = "{";
    private static final String V1_START = "%7B";
    private static final String V2_STOP = "}";
    private static final String V1_STOP = "%7D";

    private ReferenceConverter() {
    }

    /**
     * Factory method to create a {@link IUnit} instance given a persistable reference {@link String}.
     *
     * @param reference the persitable reference {@link String}.
     * @return the created {@link IUnit} instance, which is always non-null. To test whether the instance is valid, check IUnit.isValid().
     */
    public static IUnit parseUnitReference(String reference) {
        IItem raw;
        IUnit result = new Unit();
        if (reference != null) {
            String cleaned = reference.trim();
            if (cleaned.startsWith(V1_START) && cleaned.endsWith(V1_STOP)) {
                org.opengroup.osdu.core.common.model.units.impl.Unit instance = org.opengroup.osdu.core.common.model.units.impl.Unit.createInstance(cleaned);
                raw = createModel(instance);
                if (raw != null) result = (IUnit) raw;
            } else if (cleaned.startsWith(V2_START) && cleaned.endsWith(V2_STOP)) {
                PersistableReference instance = PersistableReference.createInstance(cleaned);
                raw = createModel(instance);
                if (raw instanceof IUnit) result = (IUnit) raw;
            }
        }
        return result;
    }
}
