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

package org.opengroup.osdu.core.common.crs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import org.opengroup.osdu.core.common.model.units.IUnit;
import org.opengroup.osdu.core.common.model.units.ReferenceConverter;
import org.opengroup.osdu.core.common.model.crs.ConversionRecord;
import org.opengroup.osdu.core.common.model.crs.ConvertStatus;

import java.util.Iterator;
import java.util.List;

public class UnitConversionImpl {
    private static final String KIND = "kind";
    private static final String UNIT = "unit";
    private static final String META = "meta";
    private static final String DATA = "data";
    private static final String PROPERTY_NAMES = "propertyNames";
    private static final String PERSISTABLE_REFERENCE = "persistableReference";

    public static final String NO_META_BLOCK = "Unit conversion: missing meta block.";
    public static final String MISSING_META_KIND = "Unit conversion: kind in meta block missing";
    public static final String MISSING_PROPERTY_NAMES = "Unit conversion: propertyNames missing";
    public static final String ILLEGAL_PROPERTY_NAMES = "Unit conversion: propertyNames illegal";
    public static final String MISSING_REFERENCE = "Unit conversion: persistableReference missing";
    public static final String INVALID_REFERENCE = "Unit conversion: persistableReference not valid";
    public static final String MISSING_PROPERTY = "Unit conversion: property %s missing";
    public static final String PROPERTY_VALUE_CAST_ERROR = "Unit conversion: cannot cast the value of property %s to double";
    public static final String ILLEGAL_PROPERTY_VALUE = "Unit conversion: illegal value for property %s";
    public static final String UNIT_CONVERSION_SUCCESS = "Unit conversion: success";
    public static final String UNIT_CONVERSION_FAILURE = "Unit conversion: failure";
    public static final String NO_UNIT_CONVERSION = "No unit conversion";

    public void convertUnitsToSI(List<ConversionRecord> conversionRecords) {
        for (int i = 0; i < conversionRecords.size(); i++) {
            this.convertRecordToSIUnits(conversionRecords.get(i));
        }
    }

    private void convertRecordToSIUnits(ConversionRecord conversionRecord) {
        if(null == conversionRecord) {
            return;
        }
        List<String> conversionMessages = conversionRecord.getConversionMessages();
        JsonObject record = conversionRecord.getRecordJsonObject();
        if(null == record) {
            return;
        }
        JsonArray metaArray = record.getAsJsonArray(META);
        if(null == metaArray) {
            conversionRecord.setConvertStatus(ConvertStatus.NO_FRAME_OF_REFERENCE);
            return;
        }
        boolean hasFailure = false;
        boolean isRecordUpdated = false;
        Iterator<JsonElement> metaIterator = metaArray.iterator();
        while(metaIterator.hasNext()){
            JsonObject meta = (JsonObject)metaIterator.next();
            if(null == meta){
                continue;
            }
            JsonElement kind = meta.get(KIND);
            if (null == kind) {
                hasFailure = true;
                conversionMessages.add(MISSING_META_KIND);
                continue;
            }
            if (kind.getAsString().equalsIgnoreCase(UNIT)) {
                JsonElement propertyNames = meta.get(PROPERTY_NAMES);
                if(null == propertyNames){
                    hasFailure = true;
                    conversionMessages.add(MISSING_PROPERTY_NAMES);
                    continue;
                }
                JsonArray propertyArray = null;
                try {
                    propertyArray = propertyNames.getAsJsonArray();
                }
                catch(IllegalStateException ex){
                    propertyArray = null;
                }
                if(null == propertyArray){
                    hasFailure = true;
                    conversionMessages.add(ILLEGAL_PROPERTY_NAMES);
                    continue;
                }
                JsonElement referenceElement = meta.get(PERSISTABLE_REFERENCE);
                if(null == referenceElement){
                    hasFailure = true;
                    conversionMessages.add(MISSING_REFERENCE);
                    continue;
                }
                String persistableReference = referenceElement.getAsString();
                IUnit unit = ReferenceConverter.parseUnitReference(persistableReference);
                if((null == unit) || (!unit.isValid())){
                    hasFailure = true;
                    conversionMessages.add(INVALID_REFERENCE);
                    continue;
                }
                boolean unitConverted = false;
                JsonObject data = record.getAsJsonObject(DATA);
                for(int i = 0; i < propertyArray.size(); i++) {
                    String name = propertyArray.get(i).getAsString();
                    JsonElement valueElement = data.get(name);
                    if((null == valueElement) || (valueElement instanceof JsonNull)) {
                        hasFailure = true;
                        String message = String.format(MISSING_PROPERTY, name);
                        conversionMessages.add(message);
                        continue;
                    }
                    try {
                        double value = valueElement.getAsDouble();
                        value = unit.convertToSI(value);
                        data.remove(name);
                        data.addProperty(name, value);
                        unitConverted = true;
                        isRecordUpdated = true;
                    }
                    catch(ClassCastException ccEx){
                        hasFailure = true;
                        String message = String.format(PROPERTY_VALUE_CAST_ERROR, name);
                        conversionMessages.add(message);
                    }
                    catch(IllegalStateException isEx) {
                        hasFailure = true;
                        String message = String.format(ILLEGAL_PROPERTY_VALUE, name);
                        conversionMessages.add(message);
                    }
                    catch(NumberFormatException ccEx){
                        hasFailure = true;
                        String message = String.format(ILLEGAL_PROPERTY_VALUE, name);
                        conversionMessages.add(message);
                    }
                }
                if(unitConverted && !hasFailure){
                    String basePersistableReference = unit.getBaseUnit();
                    meta.remove(PERSISTABLE_REFERENCE);
                    meta.addProperty(PERSISTABLE_REFERENCE, basePersistableReference);
                }
            }
        }
        if(hasFailure) {
            conversionMessages.add(UNIT_CONVERSION_FAILURE);
            conversionRecord.setConvertStatus(ConvertStatus.ERROR);
        }
        else {
            if(isRecordUpdated) {
                conversionRecord.setConvertStatus(ConvertStatus.SUCCESS);
            }
            else {
                conversionRecord.setConvertStatus(ConvertStatus.NO_FRAME_OF_REFERENCE);
            }
        }
        conversionRecord.setConversionMessages(conversionMessages);
    }
}
