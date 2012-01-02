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

/**
 * Calculates the average.
 * 
 * @author Lars Grammel
 */
public class AverageCalculation implements Calculation {

    /**
     * @return average value of values
     */
    public static double average(NumberArray values) {
        assert values != null;

        // TODO NaN - what is the average of an empty array?
        if (values.length() == 0) {
            return 0;
        }

        return SumCalculation.sum(values) / values.length();
    }

    @Override
    public double calculate(NumberArray values) {
        return average(values);
    }

    @Override
    public String getDescription() {
        return "Average";
    }

    @Override
    public String toString() {
        return getDescription();
    }

}