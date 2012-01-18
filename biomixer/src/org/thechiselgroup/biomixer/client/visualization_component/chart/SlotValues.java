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
package org.thechiselgroup.biomixer.client.visualization_component.chart;

import org.thechiselgroup.biomixer.client.core.util.collections.ArrayUtils;

public class SlotValues {

    private double[] values;

    private double min;

    private double max;

    private boolean maxCached = false;

    private boolean minCached = false;

    public SlotValues(double[] values) {
        assert values != null;
        this.values = values;
    }

    public double max() {
        assert values.length >= 1;

        if (!maxCached) {
            max = ArrayUtils.max(values);
            maxCached = true;
        }

        return max;
    }

    public double min() {
        assert values.length >= 1;

        if (!minCached) {
            min = ArrayUtils.min(values);
            minCached = true;
        }

        return min;
    }

    public double value(int index) {
        assert index >= 0;
        assert index < values.length;

        return values[index];
    }

    public double[] values() {
        return values;
    }

}