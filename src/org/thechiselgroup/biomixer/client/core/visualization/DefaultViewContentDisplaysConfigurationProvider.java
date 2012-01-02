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
package org.thechiselgroup.biomixer.client.core.visualization;

import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.biomixer.client.core.visualization.model.initialization.ViewContentDisplayConfiguration;
import org.thechiselgroup.biomixer.client.core.visualization.model.initialization.ViewContentDisplayFactory;
import org.thechiselgroup.biomixer.client.core.visualization.model.initialization.ViewContentDisplaysConfiguration;

import com.google.inject.Provider;

public class DefaultViewContentDisplaysConfigurationProvider implements
        Provider<ViewContentDisplaysConfiguration> {

    protected List<ViewContentDisplayConfiguration> viewContentDisplayConfigurations = new ArrayList<ViewContentDisplayConfiguration>();

    protected boolean add(ViewContentDisplayConfiguration configuration) {
        return viewContentDisplayConfigurations.add(configuration);
    }

    protected void add(ViewContentDisplayFactory factory) {
        viewContentDisplayConfigurations
                .add(new ViewContentDisplayConfiguration(factory));
    }

    @Override
    public ViewContentDisplaysConfiguration get() {
        ViewContentDisplaysConfiguration result = new ViewContentDisplaysConfiguration();
        for (ViewContentDisplayConfiguration configuration : viewContentDisplayConfigurations) {
            result.register(configuration);
        }
        return result;
    }

}