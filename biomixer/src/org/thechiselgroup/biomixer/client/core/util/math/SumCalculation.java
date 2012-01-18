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

public class SumCalculation implements Calculation {

    public static double sum(NumberArray values) {
        assert values != null;

        double sum = 0;
        for (int i = 0; i < values.length(); i++) {
            sum += values.get(i);
        }

        return sum;
    }

    @Override
    public double calculate(NumberArray values) {
        return sum(values);
    }

    @Override
    public String getDescription() {
        return "Sum";
    }

    @Override
    public String toString() {
        return getDescription();
    }

}