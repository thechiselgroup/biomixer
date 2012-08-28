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
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.visualization.View;
import org.thechiselgroup.biomixer.client.core.visualization.ViewIsReadyCondition;
import org.thechiselgroup.biomixer.client.services.term.ConceptNeighbourhoodServiceAsync;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutAlgorithm;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.tree.VerticalTreeLayoutAlgorithm;

import com.google.gwt.user.client.ui.RootPanel;
import com.google.inject.Inject;

/**
 * Loads the paths to the root for a concepts.
 * 
 * NOTE: hierarchy service not currently supported via JSONP. If it does become
 * supported later, consider re-incorporating the code from
 * {@link HierarchyPathLoader}.
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
    protected void loadData(final String virtualOntologyId,
            final String fullConceptId, final View graphView,
            ErrorHandler errorHandler) {

        /*
         * XXX NCBO REST service for hierarchy service is currently not
         * supported via JSONP. Therefore we just use the basic services
         * recursively.
         */
        loadUsingRecursiveTermService(virtualOntologyId, fullConceptId,
                graphView, errorHandler);
    }

    private void loadTerm(final String virtualOntologyId,
            final String fullConceptId, final View graphView,
            final ErrorHandler errorHandler) {

        final String conceptUri = Concept.toConceptURI(virtualOntologyId,
                fullConceptId);
        if (graphView.getResourceModel().getResources()
                .containsResourceWithUri(conceptUri)) {
            return;
        }

        conceptNeighbourhoodService.getResourceWithRelations(virtualOntologyId,
                fullConceptId, new TimeoutErrorHandlingAsyncCallback<Resource>(
                        errorHandler) {

                    @Override
                    protected String getMessage(Throwable caught) {
                        return "Could not retrieve full term information for "
                                + fullConceptId;
                    }

                    @Override
                    public void runOnSuccess(Resource resource) {

                        // hide loading bar
                        RootPanel rootPanel = RootPanel.get("loadingMessage");
                        rootPanel.setVisible(false);

                        if (graphView.getResourceModel().getResources()
                                .containsResourceWithUri(conceptUri)) {
                            return;
                        }

                        graphView.getResourceModel().getAutomaticResourceSet()
                                .add(resource);

                        for (String parentUri : resource
                                .getUriListValue(Concept.PARENT_CONCEPTS)) {

                            String parentFullConceptId = Concept
                                    .getConceptId(parentUri);
                            loadTerm(virtualOntologyId, parentFullConceptId,
                                    graphView, errorHandler);
                        }
                    }
                });
    }

    private void loadUsingRecursiveTermService(final String virtualOntologyId,
            final String fullConceptId, final View graphView,
            final ErrorHandler errorHandler) {

        executor.execute(new Runnable() {
            @Override
            public void run() {
                loadTerm(virtualOntologyId, fullConceptId, graphView,
                        errorHandler);
            }
        }, new ViewIsReadyCondition(graphView), 50);
    }

}
