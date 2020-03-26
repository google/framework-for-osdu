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

import com.google.gson.JsonObject;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class PointConversionInfo {
   private String recordId;
   private int recordIndex;
   private List<String> conversionStatus;
   private String xFieldName;
   private String yFieldName;
   private String zFieldName;
   private Double xValue;
   private Double yValue;
   private Double zValue;
   private int metaItemIndex;
   private List<JsonObject> metaItems;
   private boolean hasError;
}
