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
package org.thechiselgroup.biomixer.client.visualization_component.graph;

import java.util.List;

import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandlingAsyncCallback;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceManager;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;

/**
 * Frame for expanding neighbourhoods on {@link VisualItem}s with a single
 * {@link Resource}.
 * 
 * @author Lars Grammel
 */
public abstract class AbstractGraphNodeSingleResourceNeighbourhoodExpander<T extends ViewWithResourceManager>
        implements NodeExpander<T> {

    protected final ErrorHandler errorHandler;

    protected final ResourceManager resourceManager;

    public AbstractGraphNodeSingleResourceNeighbourhoodExpander(
            ErrorHandler errorHandler, ResourceManager resourceManager) {

        this.errorHandler = errorHandler;
        this.resourceManager = resourceManager;
    }

    @Override
    public final void expand(VisualItem visualItem,
            NodeExpansionCallback<T> graphExpansionCallback) {

        assert visualItem != null;
        assert graphExpansionCallback != null;

        Resource resource = getSingleResource(visualItem);

        if (isNeighbourhoodLoaded(visualItem, resource)) {
            expandNeighbourhood(visualItem, resource, graphExpansionCallback,
                    reconstructNeighbourhood(visualItem, resource));
        } else {
            loadNeighbourhood(visualItem, resource, graphExpansionCallback);
        }
    }

    /**
     * @param neighbourhood
     *            {@link Resource}s in neighbourhood (have been added to the
     *            resource manager already) already
     */
    protected abstract void expandNeighbourhood(VisualItem visualItem,
            Resource resource, NodeExpansionCallback<T> graph,
            List<Resource> neighbourhood);

    protected abstract String getErrorMessageWhenNeighbourhoodloadingFails(
            Resource resource, String additionalMessage);

    protected String getOntologyInfoForErrorMessage(Resource resource) {
        String ontologyName = (String) resource
                .getValue(Concept.CONCEPT_ONTOLOGY_NAME);
        if (ontologyName != null) {
            return "(" + ontologyName + ")";
        } else {
            String ontologyAcronym = (String) resource
                    .getValue(Concept.ONTOLOGY_ACRONYM);
            return "(ontology acronym: " + ontologyAcronym + ")";
        }
    }

    protected final Resource getSingleResource(VisualItem visualItem) {
        assert visualItem.getResources().size() == 1;
        return visualItem.getResources().getFirstElement();
    }

    /**
     * Checks if the required properties and resources are available.
     * 
     * @param resource
     *            TODO
     */
    protected abstract boolean isNeighbourhoodLoaded(VisualItem visualItem,
            Resource resource);

    protected abstract void loadNeighbourhood(VisualItem visualItem,
            Resource resource,
            ErrorHandlingAsyncCallback<ResourceNeighbourhood> callback);

    private void loadNeighbourhood(final VisualItem visualItem,
            final Resource resource, final NodeExpansionCallback<T> graph) {

        loadNeighbourhood(visualItem, resource,
                new ErrorHandlingAsyncCallback<ResourceNeighbourhood>(
                        errorHandler) {

                    @Override
                    protected String getMessage(Throwable caught) {
                        return getErrorMessageWhenNeighbourhoodloadingFails(
                                resource, "");
                    }

                    @Override
                    protected void runOnSuccess(ResourceNeighbourhood result)
                            throws Exception {

                        if (!graph.isInitialized()) {
                            return;
                        }

                        resource.applyPartialProperties(result
                                .getPartialProperties());
                        expandNeighbourhood(visualItem, resource, graph,
                                resourceManager.addAll(result.getResources()));
                    }

                });
    }

    protected abstract List<Resource> reconstructNeighbourhood(
            VisualItem visualItem, Resource resource);

}