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
package org.thechiselgroup.biomixer.client;

import org.thechiselgroup.choosel.core.client.util.DataType;
import org.thechiselgroup.choosel.core.client.util.collections.LightweightCollection;
import org.thechiselgroup.choosel.core.client.visualization.model.Slot;
import org.thechiselgroup.choosel.core.client.visualization.model.VisualItem;
import org.thechiselgroup.choosel.core.client.visualization.resolvers.managed.SingleSlotDependentVisualItemResolverFactory;
import org.thechiselgroup.choosel.visualization_component.graph.client.Graph;
import org.thechiselgroup.choosel.workbench.client.WorkbenchVisualItemValueResolverFactoryProvider;

import com.google.inject.Inject;

public class BioMixerVisualItemValueResolverFactoryProvider extends
        WorkbenchVisualItemValueResolverFactoryProvider {

    public static final SingleSlotDependentVisualItemResolverFactory CONCEPT_BY_ONTOLOGY_RESOLVER_FACTORY = new SingleSlotDependentVisualItemResolverFactory(
            BioMixerConceptByOntologyColorResolver.FACTORY_ID,
            new BioMixerConceptByOntologyColorResolver(), "By Ontology",
            DataType.COLOR, Graph.NODE_BACKGROUND_COLOR);

    public static final SingleSlotDependentVisualItemResolverFactory NODE_BORDER_COLOR_RESOLVER_FACTORY = new SingleSlotDependentVisualItemResolverFactory(
            "BioMixerNodeBorderColorResolver",
            new BioMixerNodeBorderColorResolver(), "Default", DataType.COLOR,
            Graph.NODE_BORDER_COLOR);

    public static final SingleSlotDependentVisualItemResolverFactory NODE_BACKGROUND_COLOR_RESOLVER_FACTORY = new SingleSlotDependentVisualItemResolverFactory(
            "biomixerNodeBackgroundcolor",
            new BioMixerNodeBackgroundColorResolver(), "Default",
            DataType.COLOR, Graph.NODE_BACKGROUND_COLOR);

    public static final SingleSlotDependentVisualItemResolverFactory GRAPH_LABEL_RESOLVER_FACTORY = new SingleSlotDependentVisualItemResolverFactory(
            "biomixer.GraphLabelResolver", new BioMixerGraphLabelResolver(),
            "Default", DataType.TEXT, Graph.NODE_LABEL_SLOT) {
        @Override
        public boolean canCreateApplicableResolver(Slot slot,
                LightweightCollection<VisualItem> visualItems) {
            return true;
        }
    };

    @Override
    @Inject
    public void registerFactories() {
        super.registerFactories();

        register(GRAPH_LABEL_RESOLVER_FACTORY);
        register(NODE_BORDER_COLOR_RESOLVER_FACTORY);
        register(NODE_BACKGROUND_COLOR_RESOLVER_FACTORY);
        register(CONCEPT_BY_ONTOLOGY_RESOLVER_FACTORY);
    }
}
