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

package org.opengroup.osdu.core.common.model.units;

import org.opengroup.osdu.core.common.model.units.impl.UnitEnergistics;
import org.opengroup.osdu.core.common.model.units.impl.UnitScaleOffset;

public class ItemFactory {
    private ItemFactory() {
    }

    public static IItem createModel(Object parsedRaw) {
        IItem result = null;
        if (parsedRaw instanceof org.opengroup.osdu.core.common.model.units.impl.Unit) {
            result = new Unit((org.opengroup.osdu.core.common.model.units.impl.Unit) parsedRaw);
        } else if (parsedRaw instanceof UnitScaleOffset) {
            result = new Unit((UnitScaleOffset) parsedRaw);
        } else if (parsedRaw instanceof UnitEnergistics) {
            result = new Unit((UnitEnergistics) parsedRaw);
        }
        return result;
    }
}
