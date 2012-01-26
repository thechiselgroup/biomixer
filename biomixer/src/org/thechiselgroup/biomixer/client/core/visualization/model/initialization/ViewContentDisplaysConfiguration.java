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

import java.util.Map;
import java.util.Set;

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.ViewContentDisplay;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemValueResolver;

public class ViewContentDisplaysConfiguration {

    private Map<String, ViewContentDisplayConfiguration> configurationMap = CollectionFactory
            .createStringMap();

    public ViewContentDisplay createDisplay(String type,
            ErrorHandler errorHandler) {
        return getConfiguration(type).createViewContentDisplay(errorHandler);
    }

    private ViewContentDisplayConfiguration getConfiguration(String type) {
        assert type != null;
        assert configurationMap.containsKey(type) : "View Content Display Configuration missing for type: "
                + type;

        return configurationMap.get(type);
    }

    public Map<Slot, VisualItemValueResolver> getFixedSlotResolvers(String type) {
        return getConfiguration(type).getFixedSlotResolvers();
    }

    public Set<String> getRegisteredTypes() {
        return configurationMap.keySet();
    }

    public void register(ViewContentDisplayConfiguration factory) {
        assert factory != null;
        configurationMap.put(factory.getViewContentTypeID(), factory);
    }

}