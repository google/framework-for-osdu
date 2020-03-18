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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class MeasurementMap {
    private static final Map<String, String> theMapSnapshot; // this contains a snapshot of the unit catalog but it is expected to change rarely.

    static {
        Map<String, String> mMap = new HashMap<>();
        mMap.put("Dimensionless", "1");
        mMap.put("1", "Dimensionless");
        mMap.put("Inverse_Temperature", "1/D");
        mMap.put("1/D", "Inverse_Temperature");
        mMap.put("Inverse_Length", "1/L");
        mMap.put("1/L", "Inverse_Length");
        mMap.put("Inverse_Area", "1/L2");
        mMap.put("1/L2", "Inverse_Area");
        mMap.put("Inverse_Volume", "1/L3");
        mMap.put("1/L3", "Inverse_Volume");
        mMap.put("Inverse_Mass", "1/M");
        mMap.put("1/M", "Inverse_Mass");
        mMap.put("Frequency", "1/T");
        mMap.put("1/T", "Frequency");
        mMap.put("Plane_Angle", "A");
        mMap.put("A", "Plane_Angle");
        mMap.put("Rotation_Per_Length", "A/L");
        mMap.put("A/L", "Rotation_Per_Length");
        mMap.put("Angle_Per_Volume", "A/L3");
        mMap.put("A/L3", "Angle_Per_Volume");
        mMap.put("Rotational_Velocity", "A/T");
        mMap.put("A/T", "Rotational_Velocity");
        mMap.put("Rotational_Acceleration", "A/T2");
        mMap.put("A/T2", "Rotational_Acceleration");
        mMap.put("Temperature", "K");
        mMap.put("K", "Temperature");
        mMap.put("Temperature_Per_Length", "D/L");
        mMap.put("D/L", "Temperature_Per_Length");
        mMap.put("Temperature_Per_Time", "D/T");
        mMap.put("D/T", "Temperature_Per_Time");
        mMap.put("Temperature_Per_Pressure", "DLT2/M");
        mMap.put("DLT2/M", "Temperature_Per_Pressure");
        mMap.put("Electric_Current", "I");
        mMap.put("I", "Electric_Current");
        mMap.put("Magnetic_Field_Strength", "I/L");
        mMap.put("I/L", "Magnetic_Field_Strength");
        mMap.put("Current_Density", "I/L2");
        mMap.put("I/L2", "Current_Density");
        mMap.put("Conductance", "I2T3/L2M");
        mMap.put("I2T3/L2M", "Conductance");
        mMap.put("Conductivity", "I2T3/L3M");
        mMap.put("I2T3/L3M", "Conductivity");
        mMap.put("Capacitance", "I2T4/L2M");
        mMap.put("I2T4/L2M", "Capacitance");
        mMap.put("Capacitance_Per_Length", "I2T4/L3M");
        mMap.put("I2T4/L3M", "Capacitance_Per_Length");
        mMap.put("Electric_Charge", "IT");
        mMap.put("IT", "Electric_Charge");
        mMap.put("Electric_Charge_Per_Volume", "IT/L3");
        mMap.put("IT/L3", "Electric_Charge_Per_Volume");
        mMap.put("Electric_Charge_Per_Mass", "IT/M");
        mMap.put("IT/M", "Electric_Charge_Per_Mass");
        mMap.put("Inverse_Electric_Potential", "IT3/L2M");
        mMap.put("IT3/L2M", "Inverse_Electric_Potential");
        mMap.put("Luminous_Intensity", "J");
        mMap.put("J", "Luminous_Intensity");
        mMap.put("Luminous_Flux", "JS");
        mMap.put("JS", "Luminous_Flux");
        mMap.put("Illuminance", "JS/L2");
        mMap.put("JS/L2", "Illuminance");
        mMap.put("Length", "L");
        mMap.put("L", "Length");
        mMap.put("Length_Per_Mass", "L/M");
        mMap.put("L/M", "Length_Per_Mass");
        mMap.put("Velocity", "L/T");
        mMap.put("L/T", "Velocity");
        mMap.put("Acceleration", "L/T2");
        mMap.put("L/T2", "Acceleration");
        mMap.put("Area", "L2");
        mMap.put("L2", "Area");
        mMap.put("Area_Per_Mass", "L2/M");
        mMap.put("L2/M", "Area_Per_Mass");
        mMap.put("Area_Per_Time", "L2/T");
        mMap.put("L2/T", "Area_Per_Time");
        mMap.put("Energy_Per_Mass", "L2/T2");
        mMap.put("L2/T2", "Energy_Per_Mass");
        mMap.put("Energy_Per_Mass_Per_Time", "L2/T3");
        mMap.put("L2/T3", "Energy_Per_Mass_Per_Time");
        mMap.put("Thermal_Transmissibility", "L2M/DT3");
        mMap.put("L2M/DT3", "Thermal_Transmissibility");
        mMap.put("Inductance", "L2M/I2T2");
        mMap.put("L2M/I2T2", "Inductance");
        mMap.put("Electric_Resistance", "L2M/I2T3");
        mMap.put("L2M/I2T3", "Electric_Resistance");
        mMap.put("Magnetic_Flux", "L2M/IT2");
        mMap.put("L2M/IT2", "Magnetic_Flux");
        mMap.put("Electric_Potential", "L2M/IT3");
        mMap.put("L2M/IT3", "Electric_Potential");
        mMap.put("Molar_Energy", "L2M/NT2");
        mMap.put("L2M/NT2", "Molar_Energy");
        mMap.put("Energy", "L2M/T2");
        mMap.put("L2M/T2", "Energy");
        mMap.put("Power", "L2M/T3");
        mMap.put("L2M/T3", "Power");
        mMap.put("Length_Per_Pressure", "L2T2/M");
        mMap.put("L2T2/M", "Length_Per_Pressure");
        mMap.put("Volume", "L3");
        mMap.put("L3", "Volume");
        mMap.put("Volume_Per_Rotation", "L3/A");
        mMap.put("L3/A", "Volume_Per_Rotation");
        mMap.put("Volume_Per_Mass", "L3/M");
        mMap.put("L3/M", "Volume_Per_Mass");
        mMap.put("Molar_Volume", "L3/N");
        mMap.put("L3/N", "Molar_Volume");
        mMap.put("Flowrate", "L3/T");
        mMap.put("L3/T", "Flowrate");
        mMap.put("Resistivity", "L3M/I2T3");
        mMap.put("L3M/I2T3", "Resistivity");
        mMap.put("Mobility", "L3T/M");
        mMap.put("L3T/M", "Mobility");
        mMap.put("Flowrate_Length", "L4/T");
        mMap.put("L4/T", "Flowrate_Length");
        mMap.put("Flowrate_Per_Pressure", "L4T/M");
        mMap.put("L4T/M", "Flowrate_Per_Pressure");
        mMap.put("Volume_Per_Pressure", "L4T2/M");
        mMap.put("L4T2/M", "Volume_Per_Pressure");
        mMap.put("Mass_Length", "LM");
        mMap.put("LM", "Mass_Length");
        mMap.put("Thermal_Conductivity", "LM/DT3");
        mMap.put("LM/DT3", "Thermal_Conductivity");
        mMap.put("Electric_Resistance_Per_Length", "LM/I2T3");
        mMap.put("LM/I2T3", "Electric_Resistance_Per_Length");
        mMap.put("Force", "LM/T2");
        mMap.put("LM/T2", "Force");
        mMap.put("Inverse_Pressure", "LT2/M");
        mMap.put("LT2/M", "Inverse_Pressure");
        mMap.put("Mass", "M");
        mMap.put("M", "Mass");
        mMap.put("Heat_Transfer", "M/DT3");
        mMap.put("M/DT3", "Heat_Transfer");
        mMap.put("Magnetic_Flux_Density_Per_Length", "M/ILT2");
        mMap.put("M/ILT2", "Magnetic_Flux_Density_Per_Length");
        mMap.put("Magnetic_Flux_Density", "M/IT2");
        mMap.put("M/IT2", "Magnetic_Flux_Density");
        mMap.put("Mass_Per_Length", "M/L");
        mMap.put("M/L", "Mass_Per_Length");
        mMap.put("Mass_Per_Area", "M/L2");
        mMap.put("M/L2", "Mass_Per_Area");
        mMap.put("Mass_Per_Area_Per_Time", "M/L2T");
        mMap.put("M/L2T", "Mass_Per_Area_Per_Time");
        mMap.put("Mass_Per_Volume", "M/L3");
        mMap.put("M/L3", "Mass_Per_Volume");
        mMap.put("Density_Per_Length", "M/L4");
        mMap.put("M/L4", "Density_Per_Length");
        mMap.put("Pressure_Per_Flowrate", "M/L4T");
        mMap.put("M/L4T", "Pressure_Per_Flowrate");
        mMap.put("Mass_Per_Length_Per_Time", "M/LT");
        mMap.put("M/LT", "Mass_Per_Length_Per_Time");
        mMap.put("Energy_Per_Volume", "M/LT2");
        mMap.put("M/LT2", "Energy_Per_Volume");
        mMap.put("Pressure", "M/LT2");
        mMap.put("Pressure_Per_Time", "M/LT3");
        mMap.put("M/LT3", "Pressure_Per_Time");
        mMap.put("Molar_Mass", "M/N");
        mMap.put("M/N", "Molar_Mass");
        mMap.put("Mass_Per_Time", "M/T");
        mMap.put("M/T", "Mass_Per_Time");
        mMap.put("Force_Per_Length", "M/T2");
        mMap.put("M/T2", "Force_Per_Length");
        mMap.put("Power_Per_Area", "M/T3");
        mMap.put("M/T3", "Power_Per_Area");
        mMap.put("Pressure_Squared", "M2/L2T4");
        mMap.put("M2/L2T4", "Pressure_Squared");
        mMap.put("Amount_Of_Substance", "N");
        mMap.put("N", "Amount_Of_Substance");
        mMap.put("Mole_Per_Volume", "N/L3");
        mMap.put("N/L3", "Mole_Per_Volume");
        mMap.put("Mole_Per_Time", "N/T");
        mMap.put("N/T", "Mole_Per_Time");
        mMap.put("Solid_Angle", "S");
        mMap.put("S", "Solid_Angle");
        mMap.put("Time", "T");
        mMap.put("T", "Time");
        mMap.put("Time_Per_Length", "T/L");
        mMap.put("T/L", "Time_Per_Length");
        mMap.put("Time_Per_Volume", "T/L3");
        mMap.put("T/L3", "Time_Per_Volume");
        mMap.put("Time_Per_Mass", "T/M");
        mMap.put("T/M", "Time_Per_Mass");
        mMap.put("Inverse_Force", "T2/LM");
        mMap.put("T2/LM", "Inverse_Force");
        mMap.put("Attenuation", "none");
        mMap.put("none", "Attenuation");
        mMap.put("Normalized_Power", "none");
        mMap.put("Attenuation_Per_Length", "none");
        mMap.put("Electric_Potential_Per_Attenuation", "none");
        mMap.put("API_Oil_Gravity", "none");
        mMap.put("API_Gamma_Ray", "none");
        mMap.put("API_Neutron", "none");
        theMapSnapshot = Collections.unmodifiableMap(mMap);
    }

    static String map(String inMeasurement) {
        if (theMapSnapshot.containsKey(inMeasurement)) return theMapSnapshot.get(inMeasurement);
        return null;
    }
}
