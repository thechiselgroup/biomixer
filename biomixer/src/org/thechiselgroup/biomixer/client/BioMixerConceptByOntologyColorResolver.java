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
package org.thechiselgroup.biomixer.client;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import org.thechiselgroup.biomixer.client.core.persistence.IdentifiableCreatingPersistence;
import org.thechiselgroup.biomixer.client.core.persistence.Memento;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.ui.Color;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemValueResolverContext;
import org.thechiselgroup.biomixer.client.core.visualization.model.managed.ManagedVisualItemValueResolver;
import org.thechiselgroup.biomixer.client.core.visualization.resolvers.AbstractBasicVisualItemValueResolver;
import org.thechiselgroup.biomixer.client.core.visualization.resolvers.managed.SingleSlotDependentVisualItemResolverFactory;

public class BioMixerConceptByOntologyColorResolver extends
        AbstractBasicVisualItemValueResolver {

    public static class Persistence implements
            IdentifiableCreatingPersistence<ManagedVisualItemValueResolver> {

        private final SingleSlotDependentVisualItemResolverFactory resolverFactory;

        public Persistence(
                SingleSlotDependentVisualItemResolverFactory resolverFactory) {
            this.resolverFactory = resolverFactory;
        }

        @Override
        public String getId() {
            return resolverFactory.getId();
        }

        @Override
        public ManagedVisualItemValueResolver restore(Memento memento) {
            for (Entry<String, Serializable> entry : memento.getValues()
                    .entrySet()) {
                // XXX buggy due to static map!
                BioMixerConceptByOntologyColorResolver.ontologyIdToColorMap
                        .put(entry.getKey(), (Color) entry.getValue());
            }

            return resolverFactory.create();
        }

        @Override
        public Memento save(ManagedVisualItemValueResolver o) {
            Memento memento = new Memento();

            for (Entry<String, Color> entry : ontologyIdToColorMap.entrySet()) {
                memento.setValue(entry.getKey(), entry.getValue());
            }

            return memento;
        }

    }

    public static final String FACTORY_ID = "biomixer.ConceptByOntologyColorResolver";

    private final static Color[] COLORS = new Color[] { new Color("#C5EFFD"),
            new Color("#BD2031"), new Color("#006295"), new Color("#A4E666"),
            new Color("#31B96E"), new Color("#616536") };

    // XXX bad hack - but colors should be the same across graph viewers and
    // they are also need for the legend.
    private static Map<String, Color> ontologyIdToColorMap = CollectionFactory
            .createStringMap();

    // XXX bad hack - but colors should be the same across graph viewers and
    // they are also need for the legend.
    public static Color getColor(String ontologyId) {
        if (!ontologyIdToColorMap.containsKey(ontologyId)) {
            Color color = COLORS[ontologyIdToColorMap.size() % COLORS.length];
            ontologyIdToColorMap.put(ontologyId, color);
        }

        return ontologyIdToColorMap.get(ontologyId);
    }

    @Override
    public boolean canResolve(VisualItem visualItem,
            VisualItemValueResolverContext context) {
        return true;
    }

    @Override
    public Object resolve(VisualItem visualItem,
            VisualItemValueResolverContext context) {
        String type = Resource.getTypeFromURI(visualItem.getId());

        if (Concept.RESOURCE_URI_PREFIX.equals(type)) {
            // XXX should be different ontologies in one node
            if (visualItem.getResources().size() > 1) {
                return new Color("#DAE5F3");
            } else {
                Resource resource = visualItem.getResources().getFirstElement();
                String ontologyId = (String) resource
                        .getValue(Concept.VIRTUAL_ONTOLOGY_ID);

                return getColor(ontologyId);
            }
        }

        if (Ontology.RESOURCE_URI_PREFIX.equals(type)) {
            // XXX should be different ontologies in one node
            if (visualItem.getResources().size() > 1) {
                return new Color("#DAE5F3");
            } else {
                Resource resource = visualItem.getResources().getFirstElement();
                String ontologyId = (String) resource
                        .getValue(Ontology.VIRTUAL_ONTOLOGY_ID);

                return getColor(ontologyId);
            }
        }

        if (Mapping.RESOURCE_URI_PREFIX.equals(type)) {
            return new Color("#E4E4E4");
        }

        // display unsupported elements in red
        return new Color("#ff0000");
    }

}