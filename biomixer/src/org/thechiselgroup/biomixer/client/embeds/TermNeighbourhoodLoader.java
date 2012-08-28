/*******************************************************************************
 * Copyright (C) 2011 Lars Grammel, Bo Fu
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

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.resources.DefaultResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.visualization.View;
import org.thechiselgroup.biomixer.client.core.visualization.ViewIsReadyCondition;
import org.thechiselgroup.biomixer.client.services.term.ConceptNeighbourhoodServiceAsync;
import org.thechiselgroup.biomixer.client.services.term.TermServiceAsync;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ResourceNeighbourhood;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutAlgorithm;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.tree.HorizontalTreeLayoutAlgorithm;

import com.google.gwt.user.client.ui.RootPanel;
import com.google.inject.Inject;

public class TermNeighbourhoodLoader extends AbstractTermGraphEmbedLoader {

    private class ConceptNeighbourhoodCallback extends
            TimeoutErrorHandlingAsyncCallback<ResourceNeighbourhood> {

        private final ResourceSet resourceSet;

        private final String fullConceptId;

        private final View graphView;

        private final Resource targetResource;

        private ConceptNeighbourhoodCallback(ErrorHandler errorHandler,
                ResourceSet resourceSet, String fullConceptId, View graphView,
                Resource targetResource) {
            super(errorHandler);
            this.resourceSet = resourceSet;
            this.fullConceptId = fullConceptId;
            this.graphView = graphView;
            this.targetResource = targetResource;
        }

        @Override
        protected String getMessage(Throwable caught) {
            return "Could not expand concept neighbourhood for "
                    + fullConceptId;
        }

        @Override
        protected void runOnSuccess(ResourceNeighbourhood targetNeighbourhood)
                throws Exception {

            // hide loading bar
            RootPanel rootPanel = RootPanel.get("loadingMessage");
            rootPanel.setVisible(false);

            targetResource.applyPartialProperties(targetNeighbourhood
                    .getPartialProperties());
            resourceSet.addAll(targetNeighbourhood.getResources());
            graphView.getResourceModel().addResourceSet(resourceSet);
        }
    }

    public static final String EMBED_MODE = "concept_neighbourhood";

    @Inject
    private TermServiceAsync termService;

    @Inject
    private ConceptNeighbourhoodServiceAsync conceptNeighbourhoodService;

    @Inject
    public TermNeighbourhoodLoader() {
        super("term neighborhood", EMBED_MODE);
    }

    private void doLoadData(final String virtualOntologyId,
            final String fullConceptId, final View graphView,
            ErrorHandler errorHandler) {
        termService.getBasicInformation(virtualOntologyId, fullConceptId,
                new TimeoutErrorHandlingAsyncCallback<Resource>(errorHandler) {

                    @Override
                    protected String getMessage(Throwable caught) {
                        return "Could not retrieve basic information for "
                                + fullConceptId;
                    }

                    @Override
                    protected void runOnSuccess(final Resource targetResource)
                            throws Exception {

                        final ResourceSet resourceSet = new DefaultResourceSet();
                        resourceSet.add(targetResource);
                        conceptNeighbourhoodService.getNeighbourhood(
                                virtualOntologyId, fullConceptId,
                                new ConceptNeighbourhoodCallback(errorHandler,
                                        resourceSet, fullConceptId, graphView,
                                        targetResource));
                    }
                });

    }

    @Override
    protected LayoutAlgorithm getLayoutAlgorithm(ErrorHandler errorHandler) {
        return new HorizontalTreeLayoutAlgorithm(true, errorHandler,
                nodeAnimator);
    }

    @Override
    protected void loadData(final String virtualOntologyId,
            final String fullConceptId, final View graphView,
            final ErrorHandler errorHandler) {
        // XXX remove once proper view content display lifecycle is available
        executor.execute(new Runnable() {
            @Override
            public void run() {
                doLoadData(virtualOntologyId, fullConceptId, graphView,
                        errorHandler);
            }
        }, new ViewIsReadyCondition(graphView), 200);
    }

}
