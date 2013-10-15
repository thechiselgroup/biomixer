/*******************************************************************************
 * Copyright 2012 David Rusk, Bo Fu 
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
package org.thechiselgroup.biomixer.client.embeds;

import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandlingAsyncCallback;
import org.thechiselgroup.biomixer.client.core.resources.DefaultResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.visualization.View;
import org.thechiselgroup.biomixer.client.core.visualization.ViewIsReadyCondition;
import org.thechiselgroup.biomixer.client.services.term.ConceptNeighbourhoodServiceAsync;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ResourceNeighbourhood;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutAlgorithm;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.tree.VerticalTreeLayoutAlgorithm;

import com.google.inject.Inject;

/**
 * Loads the paths to the root for a concepts.
 * 
 * @author drusk
 * 
 */
public class PathsToRootEmbedLoader extends AbstractTermGraphEmbedLoader {

    public static final String EMBED_MODE = "paths_to_root";

    @Inject
    private ConceptNeighbourhoodServiceAsync conceptNeighbourhoodService;

    @Inject
    public PathsToRootEmbedLoader() {
        super("path to root", EMBED_MODE);
    }

    @Override
    protected LayoutAlgorithm getLayoutAlgorithm(ErrorHandler errorHandler) {
        return new VerticalTreeLayoutAlgorithm(true, errorHandler, nodeAnimator);
    }

    @Override
    protected void loadData(final String ontologyAcronym,
            final String fullConceptId, final View graphView,
            ErrorHandler errorHandler) {

        loadUsingRecursiveTermService(ontologyAcronym, fullConceptId,
                graphView, errorHandler);
    }

    private void loadTerm(final String ontologyAcronym,
            final String fullConceptId, final View graphView,
            final ErrorHandler errorHandler) {

        final String conceptUri = Concept.toConceptURI(ontologyAcronym,
                fullConceptId);
        if (graphView.getResourceModel().getResources()
                .containsResourceWithUri(conceptUri)) {
            return;
        }

        conceptNeighbourhoodService.getPathToRootNeighbourhood(ontologyAcronym,
                fullConceptId,
                new ErrorHandlingAsyncCallback<ResourceNeighbourhood>(
                        errorHandler) {

                    @Override
                    protected String getMessage(Throwable caught) {
                        return "Could not retrieve full term information for "
                                + fullConceptId;
                    }

                    @Override
                    public void runOnSuccess(
                            ResourceNeighbourhood resourceNeighbourhood) {
                        hideLoadingBar();

                        if (graphView.getResourceModel().getResources()
                                .containsResourceWithUri(conceptUri)) {
                            return;
                        }

                        // for (Resource resource : resourceNeighbourhood
                        // .getResources()) {
                        // graphView.getResourceModel()
                        // .getAutomaticResourceSet().add(resource);
                        // }

                        // See TermNeighbourhoodLoader for some related stuff
                        final ResourceSet resourceSet = new DefaultResourceSet();
                        resourceSet.addAll(resourceNeighbourhood.getResources());
                        graphView.getResourceModel()
                                .addResourceSet(resourceSet);

                        // for (String parentUri : resource
                        // .getUriListValue(Concept.PARENT_CONCEPTS)) {
                        // // Filters out has_part relations, among others...
                        // String parentFullConceptId = Concept
                        // .getConceptId(parentUri);

                        Resource targetResource = resourceNeighbourhood
                                .getResource(Concept.toConceptURI(
                                        ontologyAcronym, fullConceptId));
                        targetResource
                                .addRelationalProperties(resourceNeighbourhood
                                        .getPartialProperties());

                        // Getting parents doesn't just get their IDs, it gets
                        // the parsable parent. So the whole paths to root
                        // neighbourhood is everything we need.
                        // loadTerm(ontologyAcronym, parentFullConceptId,
                        // graphView, errorHandler);
                        // }

                    }
                });
    }

    private void loadUsingRecursiveTermService(final String ontologyAcronym,
            final String fullConceptId, final View graphView,
            final ErrorHandler errorHandler) {

        executor.execute(new Runnable() {
            @Override
            public void run() {
                loadTerm(ontologyAcronym, fullConceptId, graphView,
                        errorHandler);
            }
        }, new ViewIsReadyCondition(graphView), 50);
    }

}
