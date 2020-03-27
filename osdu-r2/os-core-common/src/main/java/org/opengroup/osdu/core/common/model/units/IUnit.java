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

/**
 * Public interface to the Unit
 */
public interface IUnit extends IItem {
    /**
     * Get the unit symbol as a {@link String}
     *
     * @return the unit symbol as {@link String}
     */
    String getSymbol();

    /**
     * Gets the dimension of the unit as a {@link String}
     *
     * @return the dimension {@link String}
     */
    String getAncestry();

    /**
     * Gets the unit scale factor to base SI unit; formula y = scale*(x-offset).
     *
     * @return the scale factor
     */
    double getScale();

    /**
     * Gets the offset to base SI unit; formula y = scale*(x-offset).
     *
     * @return the offset
     */
    double getOffset();

    /**
     * Check whether the current {@link IUnit} instance is valid and usable for conversions.
     *
     * @return True if valid; False if any conversion will fail.
     */
    boolean isValid();

    /**
     * Checks whether unit conversion will work given another {@link IUnit}
     *
     * @param other the {@link IUnit} instance
     * @return True if conversion will succeed, False if conversion will fail.
     */
    boolean isConvertible(IUnit other);

    /**
     * Checks whether a unit conversion can be skipped (isEqualInBehavior) or not.
     *
     * @param other the {@link IUnit} instance
     * @return True if unit conversion can be skipped; False if unit conversion is required.
     */
    boolean isEqualInBehavior(IUnit other);

    /**
     * Convert a value from current unit to a specified unit.
     *
     * @param toUnit    - the target {@link IUnit} instance
     * @param fromValue - the value in the context of this instance of {@link IUnit}
     * @return the converted value in the context of toUnit or Double.NaN if this instance is invalid.
     */
    double convertToUnit(IUnit toUnit, double fromValue);

    /**
     * Convert an array of values from current unit to a specified unit.
     *
     * @param toUnit    - the target {@link IUnit} instance
     * @param fromValue - the array of values in the context of this instance of {@link IUnit}
     * @return the converted values in the context of toUnit or an array of Double.NaN if this instance is invalid.
     */
    double[] convertToUnit(IUnit toUnit, double[] fromValue);

    /**
     * Convert a value from the current unit instance to the base SI unit.
     *
     * @param fromValue - the value in the context of this instance of {@link IUnit}
     * @return - the converted value in base SI unit context or Double.NaN if this instance is invalid.
     */
    double convertToSI(double fromValue);

    /**
     * Convert an array of values in place from the current {@link IUnit} instance to SI base unit. If the conversion fails, Double.NaN values are returned.
     *
     * @param values to be unit converted in-place; If the conversion fails, Double.NaN values are returned.
     */
    void convertToSI(double[] values);

    /**
     * Convert a value from the base SI unit to the current unit instance.
     *
     * @param fromValue - the value in the context of the base SI unit
     * @return - the converted value in the context of this instance of {@link IUnit} or Double.NaN if this instance is invalid.
     */
    double convertFromSI(double fromValue);

    /**
     * Convert an array of values in place from the SI base unit to the current {@link IUnit}. If the conversion fails, Double.NaN values are returned.
     *
     * @param values to be unit converted in-place; If the conversion fails, Double.NaN values are returned.
     */
    void convertFromSI(double[] values);

    /**
     * Derives the (SI) base unit for the current unit's measurement
     *
     * @return the persistableReference string for the base unit; invalid units return null
     */
    String getBaseUnit();
}
