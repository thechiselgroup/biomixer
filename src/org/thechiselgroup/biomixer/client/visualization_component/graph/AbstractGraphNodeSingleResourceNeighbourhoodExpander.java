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

import org.thechiselgroup.choosel.core.client.error_handling.ErrorHandler;
import org.thechiselgroup.choosel.core.client.error_handling.ErrorHandlingAsyncCallback;
import org.thechiselgroup.choosel.core.client.resources.Resource;
import org.thechiselgroup.choosel.core.client.resources.ResourceManager;
import org.thechiselgroup.choosel.core.client.visualization.model.VisualItem;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Frame for expanding neighbourhoods on {@link VisualItem}s with a single
 * {@link Resource}.
 * 
 * @author Lars Grammel
 */
public abstract class AbstractGraphNodeSingleResourceNeighbourhoodExpander
        implements GraphNodeExpander {

    protected final ErrorHandler errorHandler;

    protected final ResourceManager resourceManager;

    public AbstractGraphNodeSingleResourceNeighbourhoodExpander(
            ErrorHandler errorHandler, ResourceManager resourceManager) {

        this.errorHandler = errorHandler;
        this.resourceManager = resourceManager;
    }

    @Override
    public final void expand(VisualItem visualItem,
            GraphNodeExpansionCallback graph) {

        assert visualItem != null;
        assert graph != null;

        Resource resource = getSingleResource(visualItem);

        if (isNeighbourhoodLoaded(visualItem, resource)) {
            expandNeighbourhood(visualItem, resource, graph,
                    reconstructNeighbourhood(visualItem, resource));
        } else {
            loadNeighbourhood(visualItem, resource, graph);
        }
    }

    /**
     * @param neighbourhood
     *            {@link Resource}s in neighbourhood (have been added to the
     *            resource manager already) already
     */
    protected abstract void expandNeighbourhood(VisualItem visualItem,
            Resource resource, GraphNodeExpansionCallback graph,
            List<Resource> neighbourhood);

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
            Resource resource, AsyncCallback<ResourceNeighbourhood> callback);

    private void loadNeighbourhood(final VisualItem visualItem,
            final Resource resource, final GraphNodeExpansionCallback graph) {

        loadNeighbourhood(visualItem, resource,
                new ErrorHandlingAsyncCallback<ResourceNeighbourhood>(
                        errorHandler) {

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