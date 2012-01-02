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
import java.util.Set;

import org.thechiselgroup.choosel.core.client.persistence.Memento;
import org.thechiselgroup.choosel.core.client.persistence.Persistable;
import org.thechiselgroup.choosel.core.client.persistence.PersistableObjectFactory;
import org.thechiselgroup.choosel.core.client.persistence.PersistableRestorationService;
import org.thechiselgroup.choosel.core.client.resources.Resource;
import org.thechiselgroup.choosel.core.client.resources.persistence.ResourceSetAccessor;
import org.thechiselgroup.choosel.core.client.resources.persistence.ResourceSetCollector;
import org.thechiselgroup.choosel.core.client.util.collections.CollectionFactory;
import org.thechiselgroup.choosel.core.client.util.predicates.Predicate;

public class BioMixerGraphFilterPredicate implements Predicate<Resource>,
        Persistable {

    public static class Factory implements PersistableObjectFactory {

        @Override
        public String getFactoryId() {
            return FACTORY_ID;
        }

        @Override
        public Persistable restore(Memento memento, ResourceSetAccessor accessor) {
            boolean showMappings = (Boolean) memento
                    .getValue(MEMENTO_SHOW_MAPPINGS_KEY);

            Map<String, Boolean> values = CollectionFactory.createStringMap();
            Memento child = memento.getChild(MEMENTO_VALUES_CHILD);
            for (Entry<String, Serializable> entry : child.getValues()
                    .entrySet()) {
                values.put(entry.getKey(), (Boolean) entry.getValue());
            }

            return new BioMixerGraphFilterPredicate(values, showMappings);
        }
    }

    private static final String FACTORY_ID = "biomixer.BioMixerGraphFilterPredicate";

    private static final String MEMENTO_SHOW_MAPPINGS_KEY = "showMappings";

    private static final String MEMENTO_VALUES_CHILD = "values";

    private final Map<String, Boolean> values;

    private final boolean showMappings;

    public BioMixerGraphFilterPredicate(Map<String, Boolean> values,
            boolean showMappings) {

        this.values = values;
        this.showMappings = showMappings;
    }

    @Override
    public boolean evaluate(Resource resource) {
        if (Mapping.isMapping(resource)) {
            return showMappings;
        }

        if (Concept.isConcept(resource)) {
            String ontologyId = (String) resource
                    .getValue(Concept.ONTOLOGY_ID);

            if (!values.containsKey(ontologyId)) {
                return true;
            }

            return values.get(ontologyId);
        }

        return true;
    }

    @Override
    public void restore(Memento state,
            PersistableRestorationService restorationService,
            ResourceSetAccessor accessor) {

        throw new RuntimeException("restore only using factory");
    }

    @Override
    public Memento save(ResourceSetCollector resourceSetCollector) {
        Memento memento = new Memento(FACTORY_ID);

        memento.setValue(MEMENTO_SHOW_MAPPINGS_KEY, showMappings);

        Memento child = new Memento();
        Set<Entry<String, Boolean>> entrySet = values.entrySet();
        for (Entry<String, Boolean> entry : entrySet) {
            child.setValue(entry.getKey(), entry.getValue());
        }
        memento.addChild(MEMENTO_VALUES_CHILD, child);

        return memento;
    }
}