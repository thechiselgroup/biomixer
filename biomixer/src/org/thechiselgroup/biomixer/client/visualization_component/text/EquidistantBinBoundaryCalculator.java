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
package org.thechiselgroup.biomixer.client.visualization_component.text;

import org.thechiselgroup.biomixer.client.core.util.math.NumberArray;

/**
 * Calculates <code>numberOfBins</code> equal-length bins between the minimum
 * and maximum data values.
 * 
 * @author Patrick Gorman, Bradley Blashko, Lars Grammel
 */
public class EquidistantBinBoundaryCalculator implements BinBoundaryCalculator {

    @Override
    public double[] calculateBinBoundaries(NumberArray values, int numberOfBins) {
        assert values != null;
        assert numberOfBins >= 1;

        double max = (values.isEmpty()) ? 0 : values.max();
        double min = (values.isEmpty()) ? 0 : values.min();

        assert max >= min;

        double stepSize = (max - min) / numberOfBins;

        double[] result = new double[numberOfBins - 1];
        for (int i = 0; i < numberOfBins - 1; i++) {
            result[i] = min + (stepSize * (i + 1));
        }

        return result;
    }
}
