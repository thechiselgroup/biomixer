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
package org.thechiselgroup.biomixer.client.visualization_component.graph;

import org.thechiselgroup.choosel.core.client.command.CommandManager;
import org.thechiselgroup.choosel.core.client.resources.ResourceCategorizer;
import org.thechiselgroup.choosel.core.client.resources.ResourceManager;
import org.thechiselgroup.choosel.core.client.visualization.model.ViewContentDisplay;
import org.thechiselgroup.choosel.core.client.visualization.model.initialization.ViewContentDisplayFactory;

import com.google.inject.Inject;

public class GraphViewContentDisplayFactory implements
        ViewContentDisplayFactory {

    @Inject
    private ArcTypeProvider arcStyleProvider;

    @Inject
    private CommandManager commandManager;

    @Inject
    private GraphExpansionRegistry registry;

    @Inject
    private ResourceCategorizer resourceCategorizer;

    @Inject
    private ResourceManager resourceManager;

    @Inject
    public GraphViewContentDisplayFactory() {
    }

    @Override
    public ViewContentDisplay createViewContentDisplay() {
        return new Graph(new Graph.DefaultDisplay(), commandManager,
                resourceManager, resourceCategorizer, arcStyleProvider,
                registry);
    }

    @Override
    public String getViewContentTypeID() {
        return Graph.ID;
    }
}