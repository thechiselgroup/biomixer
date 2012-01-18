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
package org.thechiselgroup.biomixer.client.core.visualization.model.initialization;

import java.util.HashMap;
import java.util.Map;

import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.ViewContentDisplay;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemValueResolver;

public class ViewContentDisplayConfiguration implements
        ViewContentDisplayFactory {

    private Map<Slot, VisualItemValueResolver> fixedSlotResolvers = new HashMap<Slot, VisualItemValueResolver>();

    private ViewContentDisplayFactory factory;

    public ViewContentDisplayConfiguration(ViewContentDisplayFactory factory) {
        this.factory = factory;
    }

    @Override
    public ViewContentDisplay createViewContentDisplay() {
        return factory.createViewContentDisplay();
    }

    public Map<Slot, VisualItemValueResolver> getFixedSlotResolvers() {
        return fixedSlotResolvers;
    }

    @Override
    public String getViewContentTypeID() {
        return factory.getViewContentTypeID();
    }

    public void setSlotResolver(Slot slot, VisualItemValueResolver resolver) {
        fixedSlotResolvers.put(slot, resolver);
    }

}