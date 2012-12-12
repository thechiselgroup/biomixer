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
import org.thechiselgroup.biomixer.client.core.ui.dialog.DialogExitCallback;
import org.thechiselgroup.biomixer.client.core.ui.dialog.DialogManager;
import org.thechiselgroup.biomixer.client.core.visualization.View;
import org.thechiselgroup.biomixer.client.core.visualization.ViewIsReadyCondition;
import org.thechiselgroup.biomixer.client.services.term.ConceptNeighbourhoodServiceAsync;
import org.thechiselgroup.biomixer.client.services.term.TermServiceAsync;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ResourceNeighbourhood;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutAlgorithm;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.radial_tree.RadialTreeLayoutAlgorithm;

import com.google.inject.Inject;

public class TermNeighbourhoodLoader extends AbstractTermGraphEmbedLoader {

    @Inject
    protected DialogManager dialogManager;

    public static final int MAX_NUMBER_OF_NEIGHBOURS = 20;

    private class ConceptNeighbourhoodCallback extends
            TimeoutErrorHandlingAsyncCallback<ResourceNeighbourhood> {

        private NeighbourCapBreachDialog neighbourBreachDialog;

        final private DialogExitCallback dialogExitCallback = new DialogExitCallback() {
            @Override
            public void dialogExited() {
                setGraphViewResources(neighbourBreachDialog.getExitCode() == NeighbourCapBreachDialog.OK_WITH_CAP_EXIT_CODE);
            }
        };

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

            hideLoadingBar();

            targetResource.applyPartialProperties(targetNeighbourhood
                    .getPartialProperties());
            resourceSet.addAll(targetNeighbourhood.getResources());

            // This is where the really slow stuff happens. One result,
            // with 381 children, took about 3 or 4 seconds to parse (just prior
            // to this part), but then takes on the order of a minute or longer
            // to render.
            // So...we can render just a few of the results (20 or so).
            // 0. Do we care about the 3 or 4 seconds to get the neighbourhood
            // parsed? Answer: Probably nothing to do, and that's not so bad
            // really.
            // 1. Can I check ahead of time how many children there are? Would
            // this help? Answer: Not really. They need to be parsed to see
            // this, which takes about as much time as a REST request to grab
            // that number might take.
            // 2. How should I restrict the number of children that get loaded?
            // Answer: Grab the default number, in whatever order. I had no
            // better alternative.
            // 3. Can I change how they are loaded to make it faster rather than
            // restricting loading? Answer: no, this appears to be a DOM
            // efficiency or browser memory issue. With 381 nodes, things are
            // very very slow to load, and slow to move all together.
            // 4. Should we somehow cache these and be able to avoid an
            // additional REST call if the user triggers expansion again?
            // Answer: Perhaps later.

            if (resourceSet.size() > MAX_NUMBER_OF_NEIGHBOURS) {
                // Callback will perform setGraphViewResources() for us.
                // setGraphViewResources(true);
                userPromptForNeighbourCap(resourceSet.size(),
                        MAX_NUMBER_OF_NEIGHBOURS);
            } else {
                setGraphViewResources(false);
            }
        }

        private void setGraphViewResources(boolean capNodes) {
            ResourceSet setToRender = resourceSet;
            if (capNodes) {
                setToRender = updateRenderedNeighboursWithMaximumNumber();
            } else {
                setToRender = resourceSet;
            }
            graphView.getResourceModel().addResourceSet(setToRender);
        }

        /**
         * This asks the user if they would like to limit the number of rendered
         * results. Since we cannot have synchronous things in GWT (JS is single
         * threaded), we are stuck using callbacks, ultimately.
         * 
         * @param setToRender
         * @return
         */
        private void userPromptForNeighbourCap(int numResources, int maxDefault) {
            neighbourBreachDialog = new NeighbourCapBreachDialog(numResources,
                    maxDefault);
            neighbourBreachDialog.setDialogExitCallback(dialogExitCallback);
            dialogManager.show(neighbourBreachDialog);
        }

        /**
         * Updates the graph view with the neighborhood, with a maximum number
         * of neighbors to enhance performance with large neighborhoods.
         * 
         * @return
         * 
         */
        private ResourceSet updateRenderedNeighboursWithMaximumNumber() {
            long i = 0;
            ResourceSet cappedSet = new DefaultResourceSet();

            for (Resource res : resourceSet) {
                cappedSet.add(res);
                i++;
                // Put break here, so we have a minimum of 1, in case max is 0.
                if (i >= MAX_NUMBER_OF_NEIGHBOURS) {
                    break;
                }
            }
            return cappedSet;
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
        // return new HorizontalTreeLayoutAlgorithm(true, errorHandler,
        // nodeAnimator);
        return new RadialTreeLayoutAlgorithm(errorHandler, nodeAnimator);
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
