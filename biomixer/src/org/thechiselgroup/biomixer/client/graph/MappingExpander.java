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
package org.thechiselgroup.biomixer.client.graph;

import org.thechiselgroup.biomixer.client.Mapping;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceManager;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.visualization_component.graph.GraphNodeExpander;
import org.thechiselgroup.biomixer.client.visualization_component.graph.GraphNodeExpansionCallback;

import com.google.inject.Inject;

/**
 * 
 * This class is for expanding concepts related to a mapping node, as opposed to
 * a concept node as in the ConceptMappingNeighbourhoodExpander.
 * 
 */
public class MappingExpander implements GraphNodeExpander {

    private final ResourceManager resourceManager;

    @Inject
    public MappingExpander(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    @Override
    public void expand(VisualItem resourceItem,
            GraphNodeExpansionCallback expansionCallback) {

        Resource mapping = resourceItem.getResources().getFirstElement();

        String sourceUri = (String) mapping.getValue(Mapping.SOURCE);
        if (!expansionCallback.containsResourceWithUri(sourceUri)) {
            if (!resourceManager.contains(sourceUri)) {
                // XXX broken, might need to call to term service?
                // --> assume available via resource manager...
                // Resource concept = new Resource(sourceUri);
                // resourceManager2.add(concept);
            }

            Resource concept = resourceManager.getByUri(sourceUri);

            expansionCallback.addAutomaticResource(concept);
        }

        String targetUri = (String) mapping.getValue(Mapping.TARGET);

        if (!expansionCallback.containsResourceWithUri(targetUri)) {
            if (!resourceManager.contains(targetUri)) {
                // XXX broken, might need to call to term service?
                // --> assume available via resource manager...
                // Resource concept = new Resource(sourceUri);
                // resourceManager2.add(concept);
            }

            Resource concept = resourceManager.getByUri(targetUri);

            expansionCallback.addAutomaticResource(concept);
        }
    }
}