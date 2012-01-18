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

//TODO think about renaming interface
public interface BinBoundaryCalculator {

    /**
     * Calculates the boundaries between different bins
     * 
     * @param values
     *            data that should be rendered in the different bins.
     * @param numberOfBins
     * 
     * @return double[] of size (numberOfBins - 1) with the suggested boundaries
     *         between the bins. It is ordered from the least to the greatest
     *         bin, meaning that result.get(0) is the boundary between the
     *         smallest and the 2nd smallest bin and that the numbers in the
     *         list are ascending. The boundary value is contained in the upper
     *         bin.
     * 
     *         If the data values are empty, all bins boundaries will be 0. If
     *         there is just one data value, all bin boundaries will be that
     *         data values. In those cases, the interval boundary will be part
     *         of the interval with the highest index and this boundary, and the
     *         other intervals with this boundary will be empty.
     */
    public double[] calculateBinBoundaries(NumberArray values, int numberOfBins);

}