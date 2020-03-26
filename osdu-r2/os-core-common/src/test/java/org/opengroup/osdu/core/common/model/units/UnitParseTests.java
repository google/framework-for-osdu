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

import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;

public class UnitParseTests {
    private static final String DEG_C_S_1 = "%7B%22ScaleOffset%22%3A%7B%22Scale%22%3A1.0%2C%22Offset%22%3A-273.15%7D%2C%22Symbol%22%3A%22degC%22%2C%22BaseMeasurement%22%3A%22%257B%2522Ancestry%2522%253A%2522Temperature%2522%257D%22%7D";
    private static final String DEG_C_E_1 = "%7B%22ABCD%22%3A%7B%22A%22%3A273.15%2C%22B%22%3A1.0%2C%22C%22%3A1.0%2C%22D%22%3A0.0%7D%2C%22Symbol%22%3A%22degC%22%2C%22BaseMeasurement%22%3A%22%257B%2522Ancestry%2522%253A%2522K%2522%257D%22%7D";
    private static final String DEG_F_S_1 = "%7B%22ScaleOffset%22%3A%7B%22Scale%22%3A0.5555555555555556%2C%22Offset%22%3A-459.67%7D%2C%22Symbol%22%3A%22degF%22%2C%22BaseMeasurement%22%3A%22%257B%2522Ancestry%2522%253A%2522Temperature%2522%257D%22%7D";
    private static final String DEG_F_E_1 = "%7B%22ABCD%22%3A%7B%22A%22%3A2298.35%2C%22B%22%3A5.0%2C%22C%22%3A9.0%2C%22D%22%3A0.0%7D%2C%22Symbol%22%3A%22degF%22%2C%22BaseMeasurement%22%3A%22%257B%2522Ancestry%2522%253A%2522K%2522%257D%22%7D";

    private static final String DEG_F_S_2 = "{\"scaleOffset\":{\"scale\":0.5555555555555556,\"offset\":-459.67},\"symbol\":\"degF\",\"baseMeasurement\":{\"ancestry\":\"Temperature\",\"type\":\"UM\"},\"type\":\"USO\"}";
    private static final String DEG_F_E_2 = "{\"abcd\":{\"a\":2298.35,\"b\":5.0,\"c\":9.0,\"d\":0.0},\"symbol\":\"degF\",\"baseMeasurement\":{\"ancestry\":\"K\",\"type\":\"UM\"},\"type\":\"UAD\"}";
    private static final String DEG_C_S_2 = "{\"scaleOffset\":{\"scale\":1.0,\"offset\":-273.15},\"symbol\":\"degC\",\"baseMeasurement\":{\"ancestry\":\"Temperature\",\"type\":\"UM\"},\"type\":\"USO\"}";
    private static final String DEG_C_E_2 = "{\"abcd\":{\"a\":273.15,\"b\":1.0,\"c\":1.0,\"d\":0.0},\"symbol\":\"degC\",\"baseMeasurement\":{\"ancestry\":\"K\",\"type\":\"UM\"},\"type\":\"UAD\"}";

    private static final String DEG_K_S_2 = "{\"scaleOffset\":{\"scale\":1.0,\"offset\":0.0},\"symbol\":\"K\",\"baseMeasurement\":{\"ancestry\":\"Temperature\",\"type\":\"UM\"},\"type\":\"USO\"}";
    private static final String M = "{\"scaleOffset\":{\"scale\":1.0,\"offset\":0},\"symbol\":\"m\",\"baseMeasurement\":{\"ancestry\":\"Length\",\"type\":\"UM\"},\"type\":\"USO\"}";

    private static final String CUSTOM_1 = "{\"scaleOffset\":{\"scale\":1000000.0,\"offset\":0.0},\"symbol\":\"MMUSD\",\"baseMeasurement\":{\"ancestry\":\"CurrencyUSD\",\"type\":\"UM\"},\"type\":\"USO\"}";

    private static final double[] DEG_FS = {32.0, 131.0};
    private static final double[] DEG_CS = {0.0, 55.0};

    private static IUnit corruptedUnit() {
        return ReferenceConverter.parseUnitReference(M.replace("{", ""));
    }

    @Test
    public void testV1PersistableRepresentation() {
        IUnit unit1 = ReferenceConverter.parseUnitReference(DEG_C_E_1);
        assertNotNull(unit1);
        assertTrue(unit1.isValid());
        IUnit unit2 = ReferenceConverter.parseUnitReference(DEG_C_S_1);
        assertNotNull(unit2);
        assertTrue(unit2.isValid());
    }

    @Test
    public void testV2PersistableRepresentation() {
        IUnit unit1 = ReferenceConverter.parseUnitReference(DEG_C_E_2);
        assertNotNull(unit1);
        assertTrue(unit1.isValid());
        IUnit unit2 = ReferenceConverter.parseUnitReference(DEG_C_S_2);
        assertNotNull(unit2);
        assertTrue(unit2.isValid());
    }

    @Test
    public void testV1V2PersistableRepresentations() {
        IUnit unit1 = ReferenceConverter.parseUnitReference(DEG_C_E_2);
        assertNotNull(unit1);
        assertTrue(unit1.isValid());
        IUnit unit2 = ReferenceConverter.parseUnitReference(DEG_C_S_1);
        assertNotNull(unit2);
        assertTrue(unit2.isValid());
    }

    @Test
    public void testUnitToUnitConversion() {
        IUnit unit1 = ReferenceConverter.parseUnitReference(DEG_C_E_1);
        IUnit unit2 = ReferenceConverter.parseUnitReference(DEG_F_E_2);

        double toValue = unit1.convertToUnit(unit2, DEG_CS[1]);
        assertEquals(DEG_FS[1], toValue, 1.0e-10);
        double[] deg_cs = DEG_CS.clone();
        double[] deg_fs = unit1.convertToUnit(unit2, deg_cs); // from DEG_C to DEG F
        for (int i = 0; i < deg_fs.length; i++) assertEquals(DEG_FS[i], deg_fs[i], 1.0e-10);
        unit2 = ReferenceConverter.parseUnitReference(DEG_F_S_2);
        toValue = unit1.convertToUnit(unit2, DEG_CS[1]);
        assertEquals(DEG_FS[1], toValue, 1.0e-10);
        deg_cs = DEG_CS.clone();
        deg_fs = unit1.convertToUnit(unit2, deg_cs); // from DEG_C to DEG F
        for (int i = 0; i < deg_fs.length; i++) assertEquals(DEG_FS[i], deg_fs[i], 1.0e-10);
        unit2 = ReferenceConverter.parseUnitReference(M);
        toValue = unit1.convertToUnit(unit2, DEG_CS[1]);
        assertEquals(Double.NaN, toValue, 1.0e-10);
        deg_cs = DEG_CS.clone();
        deg_fs = unit1.convertToUnit(unit2, deg_cs); // from DEG_C to M - must fail
        for (double deg_f : deg_fs) assertEquals(Double.NaN, deg_f, 1.0e-10);
        unit1 = ReferenceConverter.parseUnitReference(DEG_C_E_1.replace("K", "MeasurementDoesNotExist"));
        toValue = unit1.convertToUnit(unit2, DEG_CS[1]);
        assertEquals(Double.NaN, toValue, 1.0e-10);
    }

    @Test
    public void testToSIConversion() {
        double[] deg_fs = {32.0, 131.0};
        double[] deg_cs = {0.0, 55.0};
        double[] ks = {273.15, 328.15}; // expected SI

        IUnit unit = ReferenceConverter.parseUnitReference(DEG_F_S_1);
        assertConvertToAndFromSI(unit, deg_fs, ks);
        assertConvertArrayToAndFromSI(unit, deg_fs, ks);
        unit = ReferenceConverter.parseUnitReference(DEG_F_E_1);
        assertConvertToAndFromSI(unit, deg_fs, ks);
        assertConvertArrayToAndFromSI(unit, deg_fs, ks);
        unit = ReferenceConverter.parseUnitReference(DEG_C_S_1);
        assertConvertToAndFromSI(unit, deg_cs, ks);
        assertConvertArrayToAndFromSI(unit, deg_cs, ks);
        unit = ReferenceConverter.parseUnitReference(DEG_C_E_1);
        assertConvertToAndFromSI(unit, deg_cs, ks);
        assertConvertArrayToAndFromSI(unit, deg_cs, ks);
    }

    private void assertConvertToAndFromSI(IUnit unit, double[] degs, double[] sis) {
        assertNotNull(unit);
        assertTrue(unit.isValid());
        double v = degs[0];
        double si = unit.convertToSI(v);
        assertEquals(sis[0], si, 1.0e-10);
        double non_si = unit.convertFromSI(si);
        assertEquals(degs[0], non_si, 1.0e-10);
    }

    private void assertConvertArrayToAndFromSI(IUnit unit, double[] degs, double[] sis) {
        assertNotNull(unit);
        assertTrue(unit.isValid());
        double[] si = degs.clone();
        unit.convertToSI(si);
        for (int i = 0; i < sis.length; i++) assertEquals(sis[i], si[i], 1.0e-10);
        unit.convertFromSI(si);
        for (int i = 0; i < sis.length; i++) assertEquals(degs[i], si[i], 1.0e-10);
    }

    @Test
    public void testCreatePersistableReference() {
        IUnit unit = ReferenceConverter.parseUnitReference(DEG_F_E_1);
        assertRoundTripViaPersistableReference(unit);
        unit = ReferenceConverter.parseUnitReference(DEG_F_E_2);
        assertRoundTripViaPersistableReference(unit);
        unit = ReferenceConverter.parseUnitReference(DEG_C_E_1);
        assertRoundTripViaPersistableReference(unit);
        unit = ReferenceConverter.parseUnitReference(DEG_C_E_2);
        assertRoundTripViaPersistableReference(unit);
    }

    private void assertRoundTripViaPersistableReference(IUnit unit1) {
        assertNotNull(unit1);
        assertTrue(unit1.isValid());
        String pr = unit1.createPersistableReference();
        IUnit unit2 = ReferenceConverter.parseUnitReference(pr);
        assertNotNull(unit2);
        assertTrue(unit2.isValid());
        assertEquals(unit1.getScale(), unit2.getScale(), 1.0e-10);
        assertEquals(unit1.getOffset(), unit2.getOffset(), 1.0e-10);
        assertEquals(unit1.getSymbol(), unit2.getSymbol());
        assertEquals(unit1.getAncestry(), unit2.getAncestry());
    }

    @Test
    public void testInvalidPersistableReferences() {
        String pr = DEG_F_E_1.replace("%7B%22", "");
        assertFailingUnit(pr);
        IUnit unit;
        pr = DEG_F_E_2.replace("{", "");
        assertFailingUnit(pr);
    }

    private void assertFailingUnit(String pr) {
        IUnit unit = ReferenceConverter.parseUnitReference(pr);
        IUnit si = ReferenceConverter.parseUnitReference(DEG_K_S_2);
        assertNotNull(unit);
        assertFalse(unit.isValid());
        assertNull(unit.createPersistableReference());
        assertEquals(Double.NaN, unit.convertFromSI(0.0), 1.0e-10);
        assertEquals(Double.NaN, unit.convertToSI(0.0), 1.0e-10);
        assertEquals(Double.NaN, unit.convertToUnit(si, 0.0), 1.0e-10);
        double[] values = new double[]{0.0};
        unit.convertFromSI(values);
        assertEquals(Double.NaN, values[0], 1.0e-10);
        values[0] = 0.0;
        unit.convertToSI(values);
        assertEquals(Double.NaN, values[0], 1.0e-10);
        double[] deg_fs = DEG_FS.clone();
        double[] deg_cs = unit.convertToUnit(si, deg_fs);
        for (double deg_c : deg_cs) assertEquals(Double.NaN, deg_c, 1.0e-10);
    }

    @Test
    public void testEqualInBehavior() {
        IUnit degF1 = ReferenceConverter.parseUnitReference(DEG_F_S_2);
        IUnit degF2 = ReferenceConverter.parseUnitReference(DEG_F_S_2);
        IUnit kelvin = ReferenceConverter.parseUnitReference(DEG_K_S_2);
        IUnit meter = ReferenceConverter.parseUnitReference(M);
        IUnit corrupted = corruptedUnit();
        assertTrue(degF1.isEqualInBehavior(degF2));
        assertTrue(degF2.isEqualInBehavior(degF1));
        assertFalse(degF1.isEqualInBehavior(kelvin));
        assertFalse(kelvin.isEqualInBehavior(degF1));
        assertFalse(degF1.isEqualInBehavior(meter));
        assertFalse(meter.isEqualInBehavior(degF1));
        assertFalse(degF1.isEqualInBehavior(corrupted));
        assertFalse(corrupted.isEqualInBehavior(degF1));
    }

    @Test
    public void testIsConvertible() {
        IUnit degF = ReferenceConverter.parseUnitReference(DEG_F_S_2);
        IUnit kelvin = ReferenceConverter.parseUnitReference(DEG_K_S_2);
        IUnit meter = ReferenceConverter.parseUnitReference(M);
        IUnit corrupted = corruptedUnit();
        assertTrue(kelvin.isConvertible(degF));
        assertTrue(degF.isConvertible(kelvin));
        assertFalse(degF.isConvertible(meter));
        assertFalse(meter.isConvertible(degF));
        assertFalse(degF.isConvertible(corrupted));
        assertFalse(corrupted.isConvertible(degF));
    }

    @Test
    public void testGetBaseUnit() {
        IUnit degF = ReferenceConverter.parseUnitReference(DEG_F_S_2);
        String bu = degF.getBaseUnit();
        assertNotNull(bu);
        assertEquals(bu, DEG_K_S_2);
        IUnit corrupted = corruptedUnit();
        bu = corrupted.getBaseUnit();
        assertNull(bu);

        IUnit custom = ReferenceConverter.parseUnitReference(CUSTOM_1);
        bu = custom.getBaseUnit();
        assertNotNull(bu);
        IUnit custom_bu = ReferenceConverter.parseUnitReference(bu);
        double si_v = custom.convertToSI(1234.5);
        double cu_v = custom_bu.convertToUnit(custom, si_v);
        assertEquals(1234.5, cu_v, 1.0e-10);
    }
}
