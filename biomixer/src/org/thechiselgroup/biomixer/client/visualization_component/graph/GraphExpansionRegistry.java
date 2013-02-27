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

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public interface GraphExpansionRegistry {

    NodeExpander getAutomaticExpander(String category);

    NodeBulkExpander getAutomaticBulkExpander(String category);

    List<NodeMenuEntry> getNodeMenuEntries(String category);

    Set<Entry<String, List<NodeMenuEntry>>> getNodeMenuEntriesByCategory();

    void putAutomaticExpander(String category, NodeExpander expander);

    void putAutomaticBulkExpander(String category, NodeBulkExpander expander);

    void putNodeMenuEntry(String category, NodeMenuEntry nodeMenuEntry);

    void putNodeMenuEntry(String category, String label, NodeExpander expander);

    void removeAutomaticExpander(String category, Class expanderClass);

    void removeAutomaticBulkExpander(String category, Class expanderClass);

}