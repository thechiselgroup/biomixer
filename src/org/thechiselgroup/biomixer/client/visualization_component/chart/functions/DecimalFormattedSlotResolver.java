/*******************************************************************************
 * Copyright (C) 2011 Lars Grammel 
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
package org.thechiselgroup.biomixer.client.visualization_component.chart.functions;

import org.thechiselgroup.choosel.core.client.util.StringUtils;
import org.thechiselgroup.choosel.core.client.visualization.model.Slot;
import org.thechiselgroup.choosel.core.client.visualization.model.VisualItem;
import org.thechiselgroup.choosel.protovis.client.jsutil.JsArgs;
import org.thechiselgroup.choosel.protovis.client.jsutil.JsStringFunction;

public class DecimalFormattedSlotResolver implements JsStringFunction {

    private Slot slot;

    private int decimalPlaces;

    public DecimalFormattedSlotResolver(Slot slot, int decimalPlaces) {
        this.slot = slot;
        this.decimalPlaces = decimalPlaces;
    }

    @Override
    public String f(JsArgs args) {
        VisualItem visualItem = args.getObject();
        double value = visualItem.getValueAsDouble(slot);
        return StringUtils.formatDecimal(value, decimalPlaces);
    }
}