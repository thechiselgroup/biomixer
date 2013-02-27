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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;

public class DefaultGraphExpansionRegistry implements GraphExpansionRegistry {

    private Map<String, NodeExpander> automaticExpandersByCategory = CollectionFactory
            .createStringMap();

    private Map<String, NodeBulkExpander> automaticBulkExpandersByCategory = CollectionFactory
            .createStringMap();

    private Map<String, List<NodeMenuEntry>> menuEntriesByCategory = CollectionFactory
            .createStringMap();

    @Override
    public NodeExpander getAutomaticExpander(String category) {
        assert category != null;

        if (!automaticExpandersByCategory.containsKey(category)) {
            return new NullGraphNodeExpander();
        }

        return automaticExpandersByCategory.get(category);
    }

    @Override
    public NodeBulkExpander getAutomaticBulkExpander(String category) {
        assert category != null;

        if (!automaticBulkExpandersByCategory.containsKey(category)) {
            return new NullGraphNodeBulkExpander();
        }

        return automaticBulkExpandersByCategory.get(category);
    }

    @Override
    public List<NodeMenuEntry> getNodeMenuEntries(String category) {
        assert category != null;

        if (!menuEntriesByCategory.containsKey(category)) {
            return Collections.emptyList();
        }

        return menuEntriesByCategory.get(category);
    }

    @Override
    public Set<Entry<String, List<NodeMenuEntry>>> getNodeMenuEntriesByCategory() {
        return menuEntriesByCategory.entrySet();
    }

    @Override
    public void putAutomaticExpander(String category, NodeExpander expander) {
        assert category != null;
        assert expander != null;

        automaticExpandersByCategory.put(category, expander);
    }

    @Override
    public void putAutomaticBulkExpander(String category,
            NodeBulkExpander expander) {
        assert category != null;
        assert expander != null;

        automaticBulkExpandersByCategory.put(category, expander);
    }

    @Override
    public void putNodeMenuEntry(String category, NodeMenuEntry nodeMenuEntry) {
        assert category != null;
        assert nodeMenuEntry != null;

        if (!menuEntriesByCategory.containsKey(category)) {
            menuEntriesByCategory.put(category, new ArrayList<NodeMenuEntry>());
        }

        menuEntriesByCategory.get(category).add(nodeMenuEntry);
    }

    @Override
    public void putNodeMenuEntry(String category, String label,
            NodeExpander expander) {

        assert category != null;
        assert label != null;
        assert expander != null;

        putNodeMenuEntry(category, new NodeMenuEntry(label, expander));
    }

    @Override
    public void removeAutomaticExpander(String category, Class expanderClass) {
        if (automaticExpandersByCategory.get(category).getClass()
                .equals(expanderClass)) {
            automaticExpandersByCategory.remove(category);
        }
    }

    @Override
    public void removeAutomaticBulkExpander(String category, Class expanderClass) {
        if (automaticBulkExpandersByCategory.get(category).getClass()
                .equals(expanderClass)) {
            automaticBulkExpandersByCategory.remove(category);
        }
    }
}
