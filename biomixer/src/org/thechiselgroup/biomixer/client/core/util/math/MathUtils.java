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
package org.thechiselgroup.biomixer.client.core.util.math;

import com.google.gwt.core.client.GWT;

/**
 * Provides mathematical methods for NumberArrays.
 * 
 * @author Lars Grammel
 */
// TODO use object oriented representation? - superclass AbstractNumberArray?!
public final class MathUtils {

    public static NumberArray createNumberArray() {
        if (GWT.isScript() || GWT.isClient()) {
            return JsDoubleArray.create();
        }

        return new DefaultNumberArray();
    }

    public static NumberArray createNumberArray(double... values) {
        NumberArray array = createNumberArray();
        for (double value : values) {
            array.push(value);
        }
        return array;
    }

    /**
     * Deprecated - use NumberArray
     */
    @Deprecated
    public static int maxInt(int... values) {
        assert values != null;
        int max = Integer.MIN_VALUE;
        for (int value : values) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    /**
     * Deprecated - use NumberArray
     */
    @Deprecated
    public static int minInt(int... values) {
        assert values != null;
        int min = Integer.MAX_VALUE;
        for (int value : values) {
            if (value < min) {
                min = value;
            }
        }
        return min;
    }

    public static int powInt(int base, int exponent) {
        assert exponent >= 0;

        int result = 1;
        for (int i = 0; i < exponent; i++) {
            result *= base;
        }
        return result;
    }

    /**
     * Takes a requested value, and if it is not within the range [min, max]
     * then it gets set to the boundary value which it has crossed.
     * 
     * @param value
     *            the input value. If it is within the bounds, it is returned
     *            unchanged. If it is outside them, it will be 'clipped' to
     *            <code>minimum</code> or <code>maximum</code>.
     * @param minimum
     *            the minimum acceptable value
     * @param maximum
     *            the maximum acceptable value
     * @return the requested value 'clipped' to be in the specified range
     */
    public static double restrictToInterval(double value, double minimum,
            double maximum) {
        if (value < minimum) {
            return minimum;
        } else if (value > maximum) {
            return maximum;
        }
        return value;
    }

    // TODO change / refactor
    public static int restrictToInterval(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    public static double sum(double... values) {
        assert values != null;
        double sum = 0d;
        for (double value : values) {
            sum += value;
        }
        return sum;
    }

    private MathUtils() {

    }

}
