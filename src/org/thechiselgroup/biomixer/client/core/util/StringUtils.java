/*******************************************************************************
 * Copyright 2009, 2010 Lars Grammel 
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0 
 *     
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.  
 *******************************************************************************/
package org.thechiselgroup.biomixer.client.core.util;

import org.thechiselgroup.biomixer.client.core.util.math.MathUtils;

/**
 * Utility library with convenience methods for String operations.
 * 
 * @author Lars Grammel
 */
public final class StringUtils {

    public static String formatDecimal(double value, int decimalPlaces) {
        String valueAsString = Double.toString(value);

        int pointIndex = valueAsString.indexOf('.');
        if (pointIndex != -1) {
            valueAsString = valueAsString.substring(0, pointIndex);
            double decimalValue = value - Integer.parseInt(valueAsString);
            int truncatedDecimalValue = (int) (decimalValue * MathUtils.powInt(
                    10, decimalPlaces));
            return valueAsString + "." + truncatedDecimalValue;
        }

        return valueAsString + "." + StringUtils.repeat("0", decimalPlaces);
    }

    public static String repeat(String value, int times) {
        assert times >= 0;
        assert value != null;

        String result = "";
        for (int i = 0; i < times; i++) {
            result += value;
        }
        return result;
    }

    public static <T> String toString(String delimeter, T... ts) {
        if (ts.length == 0) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        result.append(ts[0]);

        for (int i = 1; i < ts.length; i++) {
            result.append(delimeter).append(ts[i]);
        }

        return result.toString();

    }

    private StringUtils() {
    }

}
