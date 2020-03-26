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

package org.opengroup.osdu.core.common.model.crs;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Point {
    private Double x;
    private Double y;
    private Double z;

    public Point() {
        setNaN(this);
    }

    public static boolean isValid(Point point) {
        if (point == null) return false;
        // if (point.x == null || point.y ==null || point.z == null) return false; // values cannot be null due to lombok constraints
        return !(Double.isNaN(point.x) || Double.isNaN(point.y) || Double.isNaN(point.z));
    }

    public static void setNaN(Point p) {
        p.x = Double.NaN;
        p.y = Double.NaN;
        p.z = Double.NaN;
    }
}
