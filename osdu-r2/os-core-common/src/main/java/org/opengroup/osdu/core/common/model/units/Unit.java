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

package org.opengroup.osdu.core.common.model.units;

import lombok.Data;
import org.opengroup.osdu.core.common.model.units.impl.Measurement;
import org.opengroup.osdu.core.common.model.units.impl.ScaleOffset;
import org.opengroup.osdu.core.common.model.units.impl.UnitEnergistics;
import org.opengroup.osdu.core.common.model.units.impl.UnitScaleOffset;

@Data
public class Unit implements IUnit {
    private static final double EPS = 1.0e-15;
    private org.opengroup.osdu.core.common.model.units.impl.Unit implementationV1;
    private UnitEnergistics implementationV2E;
    private UnitScaleOffset implementationV2S;
    private boolean valid;
    private String symbol;
    private String ancestry;
    private double scale;
    private double offset;

    public Unit() {
        initialize();
    }

    Unit(org.opengroup.osdu.core.common.model.units.impl.Unit parsedItem) {
        initialize();
        this.implementationV1 = parsedItem;
        this.valid = parsedItem.isValid();
        this.scale = parsedItem.scaleToSI();
        this.offset = parsedItem.getOffset();
        this.symbol = parsedItem.getSymbol();
        this.ancestry = parsedItem.getAncestry();
    }

    Unit(UnitEnergistics parsedItem) {
        initialize();
        this.implementationV2E = parsedItem;
        this.symbol = parsedItem.getUnitSymbol();
        this.valid = parsedItem.getBaseMeasurement() != null;
        if (this.valid) this.ancestry = parsedItem.getBaseMeasurement().getAncestry();
        if (parsedItem.getAbcd() != null) {
            this.valid = this.valid && !(Double.isNaN(parsedItem.getAbcd().getA()) || Double.isNaN(parsedItem.getAbcd().getB())
                    || Double.isNaN(parsedItem.getAbcd().getC()) || Double.isNaN(parsedItem.getAbcd().getD()));
            this.valid = this.valid && parsedItem.getAbcd().getC() != 0.0 && parsedItem.getAbcd().getB() != 0.0;
            if (this.valid) {
                this.scale = parsedItem.getAbcd().getB() / parsedItem.getAbcd().getC();
                this.offset = -parsedItem.getAbcd().getA() / parsedItem.getAbcd().getB();
            }
        }
    }

    Unit(UnitScaleOffset parsedItem) {
        initialize();
        this.implementationV2S = parsedItem;
        this.symbol = parsedItem.getUnitSymbol();
        this.valid = parsedItem.getBaseMeasurement() != null && parsedItem.getScaleOffset() != null;
        if (this.valid) {
            this.ancestry = parsedItem.getBaseMeasurement().getAncestry();
            this.valid = !(Double.isNaN(parsedItem.getScaleOffset().getScaleFactor()) ||
                    Double.isNaN(parsedItem.getScaleOffset().getOffset()) ||
                    parsedItem.getScaleOffset().getScaleFactor() == 0.0);
            this.scale = parsedItem.getScaleOffset().getScaleFactor();
            this.offset = parsedItem.getScaleOffset().getOffset();
        }
    }

    private static boolean almostEqual(double x, double y) {
        double relDiff = Math.abs(x - y) / (1.0 + 0.5 * (Math.abs(x) + Math.abs(y)));
        return (relDiff < EPS);
    }

    private void initialize() {
        this.implementationV1 = null;
        this.implementationV2E = null;
        this.implementationV2S = null;
        this.ancestry = null;
        this.valid = false;
        this.scale = Double.NaN;
        this.offset = Double.NaN;
    }

    private boolean measurementMatches(String measurement1, String measurement2) {
        boolean match = false;
        if (measurement1.equals(measurement2)) match = true;
        else {
            String other = MeasurementMap.map(measurement1);
            if (other != null && other.equals(measurement2)) match = true;
        }
        return match;
    }

    @Override
    public boolean isConvertible(IUnit other) {
        if (this.valid && other.isValid()) {
            return measurementMatches(this.ancestry, other.getAncestry());
        }
        return false;
    }

    @Override
    public boolean isEqualInBehavior(IUnit other) {
        if (this.isConvertible(other))
            return almostEqual(this.scale, other.getScale()) && almostEqual(this.offset, other.getOffset());
        return false;
    }

    @Override
    public double convertToUnit(IUnit toUnit, double fromValue) {
        if (toUnit != null && this.isValid() && toUnit.isValid() &&
                measurementMatches(this.ancestry, toUnit.getAncestry())) {
            double scale = this.getScale() / toUnit.getScale();
            double offset = this.getOffset() - toUnit.getOffset() / scale;
            return scale * (fromValue - offset);
        }
        return Double.NaN;
    }

    @Override
    public double[] convertToUnit(IUnit toUnit, double[] fromValue) {
        if (toUnit != null && this.isValid() && toUnit.isValid() &&
                measurementMatches(this.ancestry, toUnit.getAncestry())) {
            double scale = this.getScale() / toUnit.getScale();
            double offset = this.getOffset() - toUnit.getOffset() / scale;
            for (int i = 0; i < fromValue.length; i++) fromValue[i] = scale * (fromValue[i] - offset);
        } else {
            for (int i = 0; i < fromValue.length; i++) fromValue[i] = Double.NaN;
        }
        return fromValue;
    }

    @Override
    public double convertToSI(double fromValue) {
        if (this.isValid()) {
            double scale = this.getScale();
            double offset = this.getOffset();
            return scale * (fromValue - offset);
        }
        return Double.NaN;
    }

    @Override
    public double convertFromSI(double fromValue) {
        if (this.isValid()) {
            double scale = 1.0 / this.getScale();
            double offset = -this.getOffset() / scale;
            return scale * (fromValue - offset);
        }
        return Double.NaN;
    }

    @Override
    public void convertToSI(double[] fromValues) {
        if (this.isValid()) {
            double scale = this.getScale();
            double offset = this.getOffset();
            for (int i = 0; i < fromValues.length; i++) fromValues[i] = scale * (fromValues[i] - offset);
        } else {
            for (int i = 0; i < fromValues.length; i++) fromValues[i] = Double.NaN;
        }
    }

    @Override
    public void convertFromSI(double[] fromValues) {
        if (this.isValid()) {
            double scale = 1.0 / this.getScale();
            double offset = -this.getOffset() / scale;
            for (int i = 0; i < fromValues.length; i++) fromValues[i] = scale * (fromValues[i] - offset);
        } else {
            for (int i = 0; i < fromValues.length; i++) fromValues[i] = Double.NaN;
        }
    }

    @Override
    public String createPersistableReference() {
        String pr = null;
        if (this.isValid()) {
            UnitScaleOffset unit = new UnitScaleOffset();
            unit.setUnitSymbol(this.getSymbol());
            unit.setScaleOffset(new ScaleOffset(this.getOffset(), this.getScale()));
            Measurement m = new Measurement();
            m.setAncestry(this.getAncestry());
            unit.setBaseMeasurement(m);
            this.setImplementationV2S(unit);
            pr = unit.toJsonString();
        }
        return pr;
    }

    public String getBaseUnit() {
        String pr = null;
        if (this.isValid()) {
            pr = MeasurementToBaseUnit.map(this.ancestry);
            if (pr == null) { // this is a custom unit
                UnitScaleOffset u = new UnitScaleOffset();
                Measurement m = new Measurement();
                m.setAncestry(this.getAncestry());
                ScaleOffset so = new ScaleOffset();
                so.setOffset(0.0);
                so.setScaleFactor(1.0);
                String symbol = String.format("1 %s", this.getAncestry());
                u.setBaseMeasurement(m);
                u.setScaleOffset(so);
                u.setUnitSymbol(symbol);
                pr = u.toJsonString();
            }
        }
        return pr;
    }
}
