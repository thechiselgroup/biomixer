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
package org.thechiselgroup.biomixer.client.visualization_component.chart.functions;

import org.thechiselgroup.biomixer.client.core.ui.Color;
import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.choosel.protovis.client.jsutil.JsArgs;
import org.thechiselgroup.choosel.protovis.client.jsutil.JsStringFunction;

public class VisualItemColorSlotAccessor implements JsStringFunction {

    private Slot slot;

    public VisualItemColorSlotAccessor(Slot slot) {
        assert slot != null;
        this.slot = slot;
    }

    @Override
    public String f(JsArgs args) {
        VisualItem visualItem = args.getObject();
        return visualItem.<Color> getValue(slot).toRGBa();
    }

}